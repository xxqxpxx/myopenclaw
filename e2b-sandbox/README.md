# E2B OpenClaw Agent Sandbox Template

This directory contains the E2B sandbox template for OpenClaw integration with the myOpenClaw platform.

## Files

- `e2b.Dockerfile` — Docker image definition for the OpenClaw agent sandbox
- `e2b.toml` — E2B template configuration
- `README.md` — This file

## Template Features

- **Base Image**: E2B code interpreter with Python environment
- **Node.js 22**: Latest LTS version for OpenClaw runtime
- **OpenClaw**: Globally installed latest version
- **Workspace**: Pre-configured directories for output, uploads, and memory
- **Gateway**: HTTP server bound to all interfaces on port 18789
- **Environment Variables**:
  - `ANTHROPIC_API_KEY` — Required for Claude API access
  - `OPENCLAW_MODEL` — Model selection (defaults to claude-3-sonnet-20240229)

## Building and Deploying

### Prerequisites

1. Install the E2B CLI:
   ```bash
   npm install -g @e2b/cli
   ```

2. Authenticate with E2B:
   ```bash
   e2b auth login
   ```

### Deploy Template

1. Navigate to this directory:
   ```bash
   cd /Users/ahmed/Documents/openclaw/e2b-sandbox
   ```

2. Build and deploy the template:
   ```bash
   e2b template build
   ```

3. The template will be available as `openclaw-agent` for use in your E2B sandboxes.

### Usage in Code

```python
from e2b import Sandbox

# Create sandbox with OpenClaw template
sandbox = Sandbox(template="openclaw-agent", env_vars={
    "ANTHROPIC_API_KEY": "your-api-key",
    "OPENCLAW_MODEL": "claude-3-sonnet-20240229"
})

# OpenClaw gateway will be running on port 18789
# You can make HTTP requests to sandbox.get_protocol().hostname:18789
```

## Configuration

The OpenClaw configuration is stored at `/root/.openclaw/openclaw.json` and includes:

- Gateway host binding: `0.0.0.0:18789`
- Workspace paths for file operations
- Runtime environment setup

The startup script automatically configures the Anthropic API key and model from environment variables.

## Troubleshooting

- Check sandbox logs for OpenClaw startup issues
- Ensure `ANTHROPIC_API_KEY` environment variable is set
- Verify the gateway is accessible on port 18789
- OpenClaw onboard process runs silently during container startup