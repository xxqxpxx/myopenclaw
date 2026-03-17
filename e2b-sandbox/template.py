from e2b import Template

template = (
    Template()
    .from_image("e2bdev/code-interpreter:latest")
    .set_user("root")
    .set_workdir("/")
    # Install Node.js 22
    .run_cmd("curl -fsSL https://deb.nodesource.com/setup_22.x | bash - && apt-get install -y nodejs")
    # Install jq for JSON config manipulation
    .run_cmd("apt-get update && apt-get install -y jq && rm -rf /var/lib/apt/lists/*")
    # Install OpenClaw globally
    .run_cmd("SHARP_IGNORE_GLOBAL_LIBVIPS=1 npm install -g openclaw@latest")
    # Create workspace directories
    .run_cmd("mkdir -p /workspace/output /workspace/uploads /workspace/memory /root/.openclaw")
    # Create OpenClaw gateway config
    .run_cmd(
        'echo \'{"gateway":{"host":"0.0.0.0","port":18789},'
        '"workspace":"/workspace","output_dir":"/workspace/output",'
        '"uploads_dir":"/workspace/uploads","memory_dir":"/workspace/memory"}\''
        " > /root/.openclaw/openclaw.json"
    )
    # Create startup script
    .run_cmd("""cat > /root/start-openclaw.sh << 'SCRIPT'
#!/bin/bash
set -e

ANTHROPIC_API_KEY="${ANTHROPIC_API_KEY:-}"
OPENCLAW_MODEL="${OPENCLAW_MODEL:-claude-sonnet-4-20250514}"

if [ -n "$ANTHROPIC_API_KEY" ]; then
  echo "Configuring OpenClaw with API key..."
  jq --arg key "$ANTHROPIC_API_KEY" --arg model "anthropic/$OPENCLAW_MODEL" \
    '. + {"env":{"ANTHROPIC_API_KEY":$key},"agents":{"defaults":{"model":{"primary":$model}}}}' \
    /root/.openclaw/openclaw.json > /tmp/config.json && \
    mv /tmp/config.json /root/.openclaw/openclaw.json
fi

openclaw onboard --anthropic-api-key "$ANTHROPIC_API_KEY" 2>/dev/null || true
exec openclaw gateway --headless
SCRIPT
chmod +x /root/start-openclaw.sh""")
    .set_user("user")
    .set_workdir("/home/user")
    .set_start_cmd("sudo /root/start-openclaw.sh", "sleep 15")
)
