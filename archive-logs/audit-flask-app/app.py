from flask import Flask, request, jsonify
import logging, json, datetime

app = Flask(__name__)

# Operasyonel log → stdout (text)
logging.basicConfig(level=logging.INFO,
                    format="%(asctime)s %(levelname)s %(message)s")
logger = logging.getLogger("app")

# Audit log → stdout (JSON)
audit_logger = logging.getLogger("audit")
audit_logger.setLevel(logging.INFO)
audit_handler = logging.StreamHandler()
audit_handler.setFormatter(logging.Formatter("%(message)s"))
audit_logger.addHandler(audit_handler)

def emit_audit(user, action, resource, status):
    event = {
        "@timestamp": datetime.datetime.utcnow().isoformat(timespec="milliseconds") + "Z",
        "user": user,
        "action": action,
        "resource": resource,
        "status": status,
        "request_id": request.headers.get("X-Request-ID", "-"),
        "ip": request.remote_addr,
        "version": 1,
        "sourcetype": "audit"   # filtre anahtarı
    }
    audit_logger.info(json.dumps(event))

@app.route("/login", methods=["POST"])
def login():
    user = request.json.get("user")
    logger.info("login attempt for %s", user)
    emit_audit(user, "login", "self", "success")
    return jsonify(ok=True)

@app.route("/items/<item_id>")
def get_item(item_id):
    user = request.headers.get("X-User", "anonymous")
    logger.info("%s requested item %s", user, item_id)
    emit_audit(user, "read", f"item/{item_id}", "success")
    return jsonify(id=item_id, data="secret")

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8080)