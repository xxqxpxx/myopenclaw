from e2b import Template

template = (
    Template()
    .from_image("e2bdev/code-interpreter:latest")
    .set_user("root")
    .set_workdir("/")
    # Install Node.js 22
    .run_cmd("curl -fsSL https://deb.nodesource.com/setup_22.x | bash - && apt-get install -y nodejs")
    # Install jq and netcat
    .run_cmd("apt-get update && apt-get install -y jq netcat-openbsd && rm -rf /var/lib/apt/lists/*")
    # Install OpenClaw globally
    .run_cmd("SHARP_IGNORE_GLOBAL_LIBVIPS=1 npm install -g openclaw@latest")
    # Create workspace directories
    .run_cmd("mkdir -p /workspace/output /workspace/uploads /workspace/memory /root/.openclaw")
    # Write a clean minimal config (only gateway port)
    .run_cmd('echo \'{"gateway":{"port":18789}}\' > /root/.openclaw/openclaw.json')
    # Run doctor during BUILD (not at runtime) to fix config and pre-warm
    .run_cmd("openclaw doctor --fix 2>/dev/null || true")
    # Create lean startup script — just set API key and start daemon, no doctor
    .run_cmd("""cat > /root/start-openclaw.sh << 'SCRIPT'
#!/bin/bash
export ANTHROPIC_API_KEY="${ANTHROPIC_API_KEY:-}"

# Write API key directly into config file (fast, no CLI overhead)
if [ -n "$ANTHROPIC_API_KEY" ]; then
  jq --arg key "$ANTHROPIC_API_KEY" '. + {"anthropic":{"apiKey":$key}}' \
    /root/.openclaw/openclaw.json > /tmp/oc.json 2>/dev/null && \
    mv /tmp/oc.json /root/.openclaw/openclaw.json
fi

# Start gateway daemon directly
nohup openclaw daemon start > /tmp/openclaw.log 2>&1 &

# Wait for port
for i in $(seq 1 30); do
  if nc -z localhost 18789 2>/dev/null; then
    echo "Gateway ready on port 18789 (attempt $i)"
    break
  fi
  sleep 1
done

exec sleep infinity
SCRIPT
chmod +x /root/start-openclaw.sh""")
    .set_user("user")
    .set_workdir("/home/user")
    .set_start_cmd("sudo /root/start-openclaw.sh", "sleep 30")
)
