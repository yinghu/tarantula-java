[Home](README.md) > Install Notes
## Prerequisites
All instructions and samples are based on windows 10.
*NOTE: rake is no longer used.
### 1. dotnet core SDK 3.1 and runtime 3.1 and 2.1 
Download and Install dotnet SDK and Runtime
```
---- Sample SDK and RUNTIMES On Windows 10 ----  
SDK : 3.1.301 [C:\Program Files\dotnet\sdk]
RUNTIMES :
Microsoft.AspNetCore.All 2.1.19 [C:\Program Files\dotnet\shared\Microsoft.AspNetCore.All]
Microsoft.AspNetCore.App 2.1.19 [C:\Program Files\dotnet\shared\Microsoft.AspNetCore.App]
Microsoft.AspNetCore.App 3.1.5 [C:\Program Files\dotnet\shared\Microsoft.AspNetCore.App]
Microsoft.NETCore.App 2.1.19 [C:\Program Files\dotnet\shared\Microsoft.NETCore.App]
Microsoft.NETCore.App 3.1.5 [C:\Program Files\dotnet\shared\Microsoft.NETCore.App]
Microsoft.WindowsDesktop.App 3.1.5 [C:\Program Files\dotnet\shared\Microsoft.WindowsDesktop.App]
```
### 2. git cli  
### 3. docker desktop
Download docker desktop and install
*NOTE: You may need to enable vitual settings on BIOS level  
### 4. aws-vault
### 5. aws cli
### 6. prepare code bases (with-buddies-server and pantheon-server)
#### 1. Check out code bases
Both `pantheon-server` and `with-buddies-server` must be checked out into their respective directories (with the same name) within the **same root folder** (in the example below, this is **pantheon**). This ideally results in the following folder structure:

* `drive:\path-to\pantheon\pantheon-server`
* `drive:\path-to\pantheon\with-buddies-server`

Once the code is checked out into these two directories, ensure that:
#### 2. Track correct branches
* `pantheon-server` is tracking the `develop` branch
* `with-buddies-server` is tracking the `pantheon-develop` branch (**not** `develop`).
#### 3. Restore .NET packages
```
dotnet restore --configfile ..\with-buddies-server\NuGet.config Pantheon.Server.sln
```
### 7. Set up environment variables
#### 1. WB_ENV = local|Docker
*To run applications on local docker access set WB_ENV = Docker
*To run applications on remote AWS access set WB_ENV = local
#### 2. WB_AUTO_CONFIG_WORKSPACE_DIR (Optional)
You can optionally set auto-config work space. Two folders are in the workspace if you set the variable
*AutoConfig_Pantheon
*AutoConfigLocalDev_Pantheon
#### 3. WB_AUTO_CONFIG_REMOTE_DIR (Optional)
You can optionally set auto-config checkout folder. The config code is in the folder if you set the variable
*LocalDevAutoConfigRemote_Pantheon.git

## Setup pantheon-server on local docker environment
## Setup pantheon-server on remote AWS environment   

