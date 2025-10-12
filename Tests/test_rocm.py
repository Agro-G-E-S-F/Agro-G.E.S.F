import torch
import torch.nn as nn

device = "cuda" if torch.cuda.is_available() else "cpu"
print("Device:", device)

# modelo simples
model = nn.Sequential(
    nn.Conv2d(3, 16, kernel_size=3, stride=1, padding=1),
    nn.ReLU(),
    nn.Flatten(),
    nn.Linear(16*224*224, 10)
).to(device)

# dummy data
x = torch.randn(4, 3, 224, 224, device=device)
y = torch.randint(0, 10, (4,), device=device)

criterion = nn.CrossEntropyLoss()
optimizer = torch.optim.Adam(model.parameters())

# forward + backward
out = model(x)
loss = criterion(out, y)
loss.backward()
optimizer.step()

print("✅ Forward + backward rodaram sem segfault")
