# Copilot instructions for this repository

Purpose
- Help Copilot sessions understand how to build, run, and reason about this Java Swing client-server chat application.

Build / Run / Test / Lint
- ChatSystem (primary):
  - Build (full):
    - From repo root:
      mkdir -p bin && find ChatSystem -name "*.java" > sources.txt && javac -d bin @sources.txt
  - Run server (main class):
    - java -cp bin ChatSystem.server.Server
  - Run client (main entry):
    - java -cp bin ChatSystem.client.ClientManager   # client UI launched by LoginFrame in code; see client UI classes
  - Compile & run a single class (quick):
    - javac -d bin ChatSystem/server/Server.java && java -cp bin ChatSystem.server.Server

- chat_prompt_ai (alternate/smaller demo):
  - Build:
    - cd chat_prompt_ai && mkdir -p bin && javac -d bin src/chatprompt/common/*.java src/chatprompt/server/*.java src/chatprompt/client/*.java
  - Run server:
    - cd chat_prompt_ai && java -cp bin chatprompt.server.ChatServer
  - Run client:
    - cd chat_prompt_ai && java -cp bin chatprompt.client.ChatClient

- Tests / Lint
  - No automated unit tests or linters are present in the repository. "Single test" means running a single main-class scenario (see "Compile & run a single class" above).

High-level architecture (big picture)
- Two related Java Swing client-server projects live in the tree:
  1. ChatSystem/ — main app (server + Swing client + DB integration).
  2. chat_prompt_ai/ — a smaller, self-contained demo with similar client/server code and run scripts.
- ChatSystem package layout (important packages):
  - ChatSystem.server — Server entrypoint, ClientHandler, MessageRouter, SessionManager.
  - ChatSystem.client — ClientManager and Swing UI (LoginFrame, MainFrame, panels).
  - ChatSystem.shared — Serializable Message and MessageType used as the protocol across socket streams.
  - ChatSystem.server.dao — DAO classes (UserDAO, GroupDAO, MessageDAO) that persist chat history.
  - ChatSystem.database — DBConnection and db.properties; schema.sql contains the schema.
- Communication model: Java serialization over TCP sockets using ObjectInputStream/ObjectOutputStream and Message objects (MessageType enum drives behavior). Server listens on TCP port 12345 by default.
- Persistence: PostgreSQL is used. DB config is stored at ChatSystem/database/db.properties (do not commit secrets). Schema: ChatSystem/database/schema.sql.

Key conventions and patterns
- Protocol objects:
  - Message implements Serializable and carries type, sender, target/group, file payloads, content, timestamp, and optional DB id. Copilot should treat fields like getMessageType(), isSuccess(), getContent() as central.
- Message routing:
  - Server constructs MessageRouter(sessionManager, userDAO, groupDAO, messageDAO) — most business logic & routing is here.
- DAO pattern:
  - DAO classes live under server/dao and encapsulate DB SQL operations; method names are straightforward (insert, update, find, delete).
- UI threading:
  - Swing UI updates are invoked via SwingUtilities.invokeLater(...) throughout ClientManager and UI classes. Generate changes that preserve EDT usage.
- Port and configuration:
  - Default server port is 12345; database config at ChatSystem/database/db.properties. Avoid hard-coding credentials into PRs.
- Compiled/output directories
  - Compiled classes and IDE metadata appear under bin/, out/, and .idea/; these are build artifacts and not a source of truth.

Relevant docs to incorporate
- README.md (root) and chat_prompt_ai/README.md contain quick-start commands. Merge those into suggestions above when appropriate.

Notes for Copilot suggestions
- Prefer changes that preserve the existing socket-object-serialization protocol unless the user explicitly migrates to a different protocol.
- When adding code that touches the UI, ensure Swing EDT rules are followed (use invokeLater or SwingWorker).
- When modifying DB access, update DAO methods and keep SQL in ChatSystem/server/dao and schema.sql in sync.
- Don't suggest committing secrets; if code references db.properties, indicate the path and recommend using environment variables or a secrets mechanism instead.

If you want changes to this file or coverage for additional areas (e.g., deployment, CI, or tests), say which area to expand.

Session logging (prompt / summary / timestamp)
- Purpose: record each interactive prompt, a short AI-generated summary, and the session timestamp in the repository so future sessions can reference prior troubleshooting steps.
- Script (included): .github/copilot-log.sh
  - Make executable: chmod +x .github/copilot-log.sh
  - Append format (example):
    * * 15/04/2026 09:30	prompt **"tại sao tôi nhập tên đăng nhập rồi mà không có gì xảy ra tiếp theo cả, kể cả thông báo lỗi"**	Gỡ lỗi (Debugging) luồng sự kiện ngầm (Silent fail).
  - How to use (manual): after a prompt, run:
    .github/copilot-log.sh "<exact-prompt-text>" "<short-summary>"
  - Log location: .github/copilot_session_log.md (created by the script on first write)
- Automatic logging (recommended):
  - Option A (ask the assistant to persist): include in your prompt a directive that the assistant must run the logger at the end of its reply, e.g. "When you finish, run .github/copilot-log.sh \"<prompt>\" \"<short-summary>\" and avoid logging any secrets." The assistant can then call the script when given permission.
  - Option B (Copilot CLI scheduling): use the Copilot CLI scheduling commands to run the logger for prompts you supply: e.g. `/after 0s .github/copilot-log.sh "<prompt-text>" "<short-summary>"` or use `/every` for periodic logging of supplied prompts.
  - Option C (local wrapper): create a local wrapper that sends the prompt to the assistant and then calls this script with the assistant's summary (example wrapper not included).
  - Security: do not log secrets or credentials. Review entries before committing.
- Guidance for Copilot sessions:
  - When summarizing or replying to a user's prompt, append a one-line summary and run the provided script to persist the entry, or include the formatted line in the session notes.
  - Keep summaries concise (one short phrase). The timestamp is generated by the script.
- Privacy: avoid logging secrets or credentials; do not send db.properties values or other secrets into the log.


