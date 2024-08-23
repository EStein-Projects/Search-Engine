# Document Store and Search Engine
### Semester Project for Data Structures (COM 1320), Spring 2024

#### [Stage 1](stage1/src/main/java/edu/yu/cs/com1320/searchengine): In-Memory Search Engine
- In-memory store for text and media documents
- Search functionality:
    - Text documents by keyword
    - All documents by metadata
- Support for reversing:
    - Additions to the store
    - Deletions from the store
    - Document metadata modifications
- Implemented a hash table for efficient storage and retrieval

#### [Stage 2](stage2/src/main/java/edu/yu/cs/com1320/searchengine): Hybrid Search Engine
- Adaptive storage for text and media documents, storing recently used ones in RAM for fast access and the rest on disk
- Search functionality:
    - Text documents by keyword
    - All documents by metadata
- Support for reversing:
    - Additions to the store
    - Deletions from the store
    - Document metadata modifications
- Utilized a heap for memory management and a B-tree for optimized storage and retrieval

#### Testing Framework:
- JUnit

#### Data Structures Implemented:
- Stack
- Trie
- Hash table (Stage 1))
- Heap (Stage 2)
- B-Tree (Stage 2)
