param(
    [string]$BaseUrl = "http://localhost:8888",
    [string]$Username = "flowdesk-local-owner",
    [string]$Password = "local-only-bootstrap-password"
)

$ErrorActionPreference = "Stop"

function Fail {
    param([string]$Message)
    Write-Host "FAIL $Message" -ForegroundColor Red
    exit 1
}

function Pass {
    param([string]$Message)
    Write-Host "PASS $Message" -ForegroundColor Green
}

function Invoke-Json {
    param(
        [string]$Method,
        [string]$Path,
        [object]$Body = $null,
        [string]$Token = $null
    )

    $headers = @{}
    if ($Token) {
        $headers["Authorization"] = "Bearer $Token"
    }

    $uri = "$BaseUrl$Path"
    if ($Body -ne $null) {
        $json = $Body | ConvertTo-Json -Depth 20
        return Invoke-RestMethod -Method $Method -Uri $uri -Headers $headers -ContentType "application/json" -Body $json
    }

    return Invoke-RestMethod -Method $Method -Uri $uri -Headers $headers
}

try {
    $health = Invoke-Json -Method "GET" -Path "/actuator/health"
    if (-not $health.status) {
        Fail "health response did not include status"
    }
    Pass "GET /actuator/health"

    $login = Invoke-Json -Method "POST" -Path "/v1/user/login" -Body @{
        name = $Username
        password = $Password
    }
    $token = $login.data.token
    if (-not $token) {
        Fail "login response did not include data.token"
    }
    Pass "POST /v1/user/login"

    $initialize = Invoke-Json -Method "POST" -Path "/v1/mcp/jsonrpc" -Token $token -Body @{
        jsonrpc = "2.0"
        id = "demo-initialize"
        method = "initialize"
        params = @{
            protocolVersion = "2025-06-18"
        }
    }
    if ($initialize.result.serverInfo.name -ne "flowdesk") {
        Fail "initialize did not return Flowdesk serverInfo"
    }
    Pass "MCP initialize"

    $ping = Invoke-Json -Method "POST" -Path "/v1/mcp/jsonrpc" -Token $token -Body @{
        jsonrpc = "2.0"
        id = "demo-ping"
        method = "ping"
        params = @{}
    }
    if ($null -eq $ping.result) {
        Fail "ping did not return a result object"
    }
    Pass "MCP ping"

    $tools = Invoke-Json -Method "POST" -Path "/v1/mcp/jsonrpc" -Token $token -Body @{
        jsonrpc = "2.0"
        id = "demo-tools-list"
        method = "tools/list"
        params = @{}
    }
    $toolNames = @($tools.result.tools | ForEach-Object { $_.name })
    if ($toolNames -notcontains "flowdesk_search_knowledge") {
        Fail "tools/list missing flowdesk_search_knowledge"
    }
    if ($toolNames -notcontains "flowdesk_upload_document_metadata") {
        Fail "tools/list missing flowdesk_upload_document_metadata"
    }
    Pass "MCP tools/list"

    $metadata = Invoke-Json -Method "POST" -Path "/v1/mcp/jsonrpc" -Token $token -Body @{
        jsonrpc = "2.0"
        id = "demo-upload-metadata"
        method = "tools/call"
        params = @{
            name = "flowdesk_upload_document_metadata"
            arguments = @{
                fileName = "sample-employee-handbook.pdf"
                contentType = "application/pdf"
                sizeBytes = 123456
            }
        }
    }
    if ($metadata.result.structuredContent.success -ne $true) {
        Fail "flowdesk_upload_document_metadata did not succeed"
    }
    Pass "MCP flowdesk_upload_document_metadata"

    $writeDisabled = Invoke-Json -Method "POST" -Path "/v1/mcp/jsonrpc" -Token $token -Body @{
        jsonrpc = "2.0"
        id = "demo-create-todo-disabled"
        method = "tools/call"
        params = @{
            name = "flowdesk_create_todo"
            arguments = @{
                title = "Prepare local demo"
            }
        }
    }
    if ($writeDisabled.result.isError -ne $true) {
        Fail "flowdesk_create_todo was not returned as an error while write tools are disabled"
    }
    if ($writeDisabled.result.structuredContent.error.code -ne "WRITE_TOOLS_DISABLED") {
        Fail "flowdesk_create_todo did not return WRITE_TOOLS_DISABLED"
    }
    Pass "MCP write-disabled guard"

    $citations = Invoke-Json -Method "POST" -Path "/v1/knowledge/chat-with-citations" -Token $token -Body @{
        prompts = "What should employees do before taking leave?"
        chatType = 0
        relationId = "demo-smoke"
    }
    if (-not ($citations.data.PSObject.Properties.Name -contains "citations")) {
        Fail "chat-with-citations response did not include data.citations"
    }
    Pass "RAG citations response shape"

    Write-Host "PASS Flowdesk demo smoke checks completed" -ForegroundColor Green
    exit 0
} catch {
    Fail $_.Exception.Message
}
