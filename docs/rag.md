# RAG and Knowledge Retrieval

Flowdesk includes a minimal RAG path for PDF-based knowledge retrieval using the configured AI provider for embeddings and Redis Stack / RediSearch for vector search.

## Flow

```mermaid
flowchart LR
    Upload["Upload PDF"] --> PDFBox["Extract text with PDFBox"]
    PDFBox --> Clean["Clean text"]
    Clean --> Chunk["Split into chunks"]
    Chunk --> Embed["Provider embeddings"]
    Embed --> Redis["Redis Stack vector index"]
    Query["User question"] --> QueryEmbed["Provider query embedding"]
    QueryEmbed --> Search["RediSearch vector search"]
    Search --> Context["Relevant chunks"]
    Context --> Chat["AI chat response"]
```

## Components

| Component | Package | Notes |
| --- | --- | --- |
| PDF parsing | `ai.knowledge.PDFProcessor` | Extracts text and splits content into chunks |
| Vector store | `ai.knowledge.VectorStoreService` | Handles Redis Stack / RediSearch storage and lookup |
| AI tools | `ai.tools.KnowledgeTools` | Exposes knowledge retrieval to AI tool calls |
| Diagnostics | `controller.KnowledgeDiagController` | Dev-only status/search diagnostics |

## Local Demo Asset

Use `docs/examples/sample-employee-handbook.pdf` for local demos. It is synthetic placeholder material and should not be replaced with real company documents in public commits.

## Requirements

- Redis Stack running on `localhost:6379`
- `FLOWDESK_AI_ENABLED=true`
- `FLOWDESK_AI_PROVIDER=ollama` with local Ollama running, or `FLOWDESK_AI_PROVIDER=dashscope` with `DASHSCOPE_API_KEY`
- A compatible embedding model. Ollama defaults to `nomic-embed-text`; DashScope defaults to `text-embedding-v3`.

## Provider Notes

Ollama mode does not require an API key:

```powershell
ollama pull qwen2.5:7b
ollama pull nomic-embed-text
ollama serve

$env:FLOWDESK_AI_ENABLED="true"
$env:FLOWDESK_AI_PROVIDER="ollama"
```

DashScope mode requires a real key:

```powershell
$env:FLOWDESK_AI_ENABLED="true"
$env:FLOWDESK_AI_PROVIDER="dashscope"
$env:DASHSCOPE_API_KEY="your-dashscope-api-key"
```

Vector dimensions must match the Redis Stack index. Flowdesk defaults Ollama indexes to `knowledge_java_ollama_768` and keeps DashScope on `knowledge_java`. If you change `OLLAMA_EMBEDDING_MODEL`, `DASHSCOPE_EMBEDDING_MODEL`, or `FLOWDESK_AI_EMBEDDING_DIMENSION`, rebuild the knowledge index.

## Current Limitations

- This is a backend template, not a managed knowledge-base product.
- Index lifecycle and migration flows are intentionally minimal.
- Production deployments should add access control, document ownership, audit logging, and cleanup jobs.
- Large document ingestion should be moved to background processing before production use.
