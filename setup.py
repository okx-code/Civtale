from pathlib import Path
import subprocess
import platform
import shutil
import urllib.request
import zipfile
import os

if platform.system() == "Linux":
    is_linux = True
elif platform.system() == "Windows":
    is_linux = False
else:
    print("Unsupported platform")
    exit(1)

temp = Path("temp")

server = temp / "server"
ext = temp / "extracted"
classes = temp / "classes"

try:
    shutil.rmtree("temp")
except FileNotFoundError:
    pass
os.mkdir("temp")

print("Downloading hytale downloader...")
req = urllib.request.Request("https://downloader.hytale.com/hytale-downloader.zip", headers={"User-Agent": "civ"})
with urllib.request.urlopen(req) as response, open("temp/hytale-downloader.zip", "wb") as f:
    f.write(response.read())

print("Extracting...")
with zipfile.ZipFile("temp/hytale-downloader.zip", 'r') as zip_ref:
    zip_ref.extractall("temp")

print("Running downloader...")

if is_linux:
    downloader = temp / "hytale-downloader-linux-amd64"
    mode = os.stat(downloader).st_mode
    os.chmod(downloader, mode | 0o100)
else:
    downloader = temp / "hytale-downloader-windows-amd64.exe"

os.mkdir(server)
subprocess.run([downloader.absolute()], check=True, cwd=(server))

for file in os.listdir(server):
    if file.endswith(".zip"):
        hytale_server = server / file
        break

if hytale_server is None:
    print("Could not find downloaded server")
    exit(1)

print("Extracting files...")
os.mkdir(ext)
with zipfile.ZipFile(hytale_server, 'r') as zip_ref:
    zip_ref.extractall(ext)

print("Updating server...")
os.makedirs("server", exist_ok=True)
shutil.copyfile(ext / "Assets.zip", Path("server") / "Assets.zip")
shutil.copyfile(ext / "Server/HytaleServer.aot", Path("server") / "HytaleServer.aot")
shutil.copyfile(ext / "Server/HytaleServer.jar", Path("server") / "HytaleServer.jar")

hytale_server_jar = Path("server") / "HytaleServer.jar"
fernflower_jar = Path("fernflower") / "build" / "libs" / "fernflower.jar"
sources = Path("sources")


print("Decompiling server...")
try:
    shutil.rmtree("fernflower")
except FileNotFoundError:
    pass
os.mkdir("fernflower")
req = urllib.request.Request("https://github.com/JetBrains/fernflower/archive/refs/heads/master.zip", headers={"User-Agent": "civ"})
with urllib.request.urlopen(req) as response, open("temp/fernflower.zip", "wb") as f:
    f.write(response.read())
with zipfile.ZipFile("temp/fernflower.zip", 'r') as zip_ref:
    zip_ref.extractall("fernflower")
if is_linux:
    subprocess.run([(Path("fernflower") / "gradlew").absolute(), "build"], check=True, cwd="fernflower")
else:
    subprocess.run([(Path("fernflower") / "gradlew.bat").absolute(), "build"], check=True, cwd="fernflower")
try:
    shutil.rmtree("sources")
except FileNotFoundError:
    pass
os.mkdir("sources")
with zipfile.ZipFile(hytale_server_jar, 'r') as zip_ref:
    zip_ref.extractall(classes)
subprocess.run(["java", "-jar", fernflower_jar.absolute(), "-dgs=1", classes.absolute(), sources.absolute()], check=True)

print("Cleaning up")
try:
    shutil.rmtree("fernflower")
except FileNotFoundError:
    pass
try:
    shutil.rmtree("temp")
except FileNotFoundError:
    pass
print("Done!")
