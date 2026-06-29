# RAG Quality Lab

Flowdesk includes a small offline RAG quality helper for smoke-testing retrieval quality without calling DashScope, Ollama, MongoDB, or Redis.

The lab is intentionally simple. It checks whether a known query retrieves the expected document, whether retrieved snippets contain expected keywords, and whether citations can be produced from the retrieved chunks.

## Dataset Shape

Example cases live in `src/test/resources/rag/eval-cases.json`:

```json
[
  {
    "query": "What should employees do before taking leave?",
    "expectedKeywords": ["leave", "manager"],
    "expectedSourceDocument": "sample-employee-handbook.pdf",
    "notes": "Synthetic smoke case for the public demo handbook."
  }
]
```

Fields:

- `query`: user question to evaluate.
- `expectedKeywords`: words that should appear in returned snippets.
- `expectedSourceDocument`: source document expected in the top-K retrieval set.
- `notes`: human-readable reason for the case.

## What It Measures

`RagQualityLabEvaluator` evaluates already-retrieved documents. It does not call an embedding model or vector database.

The current checks are:

- `targetDocumentFound`: at least one top-K result source contains `expectedSourceDocument`.
- `expectedKeywordsFound`: all expected keywords appear across returned chunk content.
- `citationPresent`: retrieved chunks can be mapped to at least one citation.
- `passed`: all checks above are true.

## Running The Offline Tests

```powershell
.\mvnw.cmd "-Dtest=*Rag*,*Citation*,*Quality*" test
```

These tests use synthetic in-memory chunks only. They are suitable for CI and public repositories because they do not require real model credentials or private documents.

## Interpreting Results

Passing offline tests mean the evaluator, citation mapping, and example dataset format are working. They do not prove that a live embedding model retrieves perfect answers. Real RAG quality still depends on:

- the uploaded document content,
- chunk size and overlap,
- embedding provider and model,
- Redis Stack index health,
- prompt and answer generation behavior.
