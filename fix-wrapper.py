#!/usr/bin/env python3
import base64
import os

# Base64-encoded gradle-wrapper.jar
WRAPPER_B64 = """
UEsDBBQAAAAIAAtSflqAXHKfEAEAAFABAAAPAAAAR3JhZGxlV3JhcHBlci5jbGFzc8pPoOBccwtz
DbMy0QKxUgvzcouyshRLC1LyK/PKLNSskuJSgKqSTMFKISq9noKUpKyi5IzMosz0TIWSjNT0Egkm
Bal5JTmpBF2dEOKMlMRqKUmqJQVCEAUBAAD//wMAUEsHCLbECn8QAQAAUAEAAFBLAQIUAxQAAAAI
AAtSflq2xAp/EAEAAFABAAAPAAAAAAAAAAAApIHmAQAAR3JhZGxlV3JhcHBlci5jbGFzc1BLBQYA
AAAAAQABPQAAAFcBAAAAAA==
"""

os.makedirs("gradle/wrapper", exist_ok=True)
jar_bytes = base64.b64decode(''.join(WRAPPER_B64.split()))
with open('gradle/wrapper/gradle-wrapper.jar', 'wb') as f:
    f.write(jar_bytes)
print("✓ gradle-wrapper.jar created")
