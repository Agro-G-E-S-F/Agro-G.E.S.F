import os
os.environ['MIOPEN_DEBUG_CONV_FALLBACK']='1'   # força kernels fallback
os.environ['PYTORCH_HIP_ALLOC_CONF']='expandable_segments:True'  # mais estável em algumas configs

import torch, torch.nn as nn
device = "cuda" if torch.cuda.is_available() else "cpu"
print("Device ->", device, torch.version.hip)

m = nn.Sequential(
    nn.Conv2d(3, 16, 3, padding=1),
    nn.ReLU(),
    nn.AdaptiveAvgPool2d(1),
    nn.Flatten(),
    nn.Linear(16, 10)
).to(device)

x = torch.randn(4,3,224,224, device=device)
y = torch.randint(0,10,(4,), device=device)

try:
    out = m(x)
    loss = nn.CrossEntropyLoss()(out, y)
    loss.backward()
    print("FORWARD + BACKWARD ok")
except Exception as e:
    import traceback; traceback.print_exc()
    print("Erro no forward/backward:", e)
