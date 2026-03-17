# E2B OpenClaw Agent Sandbox Template
FROM e2b/code-interpreter:latest

# Install Node.js 22
RUN curl -fsSL https://deb.nodesource.com/setup_22.x | sudo -E bash - && \
    sudo apt-get install -y nodejs

# Install OpenClaw globally
RUN npm install -g openclaw@latest

# Create workspace directories
RUN mkdir -p /workspace/output /workspace/uploads /workspace/memory

# Create OpenClaw config directory
RUN mkdir -p /root/.openclaw

# Create OpenClaw config file with gateway binding on all interfaces
RUN echo '{\
  "gateway": {\
    "host": "0.0.0.0",\
    "port": 18789\
  },\
  "workspace": "/workspace",\
  "output_dir": "/workspace/output",\
  "uploads_dir": "/workspace/uploads",\
  "memory_dir": "/workspace/memory"\
}' > /root/.openclaw/openclaw.json

# Create startup script
RUN echo '#!/bin/bash\n\
set -e\n\
\n\
# Read environment variables\n\
ANTHROPIC_API_KEY=${ANTHROPIC_API_KEY:-""}\n\
OPENCLAW_MODEL=${OPENCLAW_MODEL:-"claude-3-sonnet-20240229"}\n\
\n\
# Update OpenClaw config with environment variables\n\
if [ -n "$ANTHROPIC_API_KEY" ]; then\n\
  echo "Configuring OpenClaw with Anthropic API key..."\n\
  # Update config with API key and model\n\
  jq --arg key "$ANTHROPIC_API_KEY" --arg model "$OPENCLAW_MODEL" \\\n\
    ".anthropic = {\"api_key\": $key, \"model\": $model}" \\\n\
    /root/.openclaw/openclaw.json > /tmp/config.json && \\\n\
    mv /tmp/config.json /root/.openclaw/openclaw.json\n\
fi\n\
\n\
echo "Running OpenClaw onboard..."\n\
# Run onboard silently\n\
openclaw onboard --silent 2>/dev/null || echo "Onboard completed"\n\
\n\
echo "Starting OpenClaw gateway..."\n\
# Start gateway in headless mode\n\
exec openclaw gateway --headless\n\
' > /root/start-openclaw.sh

# Make startup script executable
RUN chmod +x /root/start-openclaw.sh

# Install jq for JSON manipulation
RUN apt-get update && apt-get install -y jq && rm -rf /var/lib/apt/lists/*

# Expose the gateway port
EXPOSE 18789

# Set default command to start OpenClaw
CMD ["/root/start-openclaw.sh"]