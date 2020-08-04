[Home](README.md) > Install Notes
# Prerequisites
### 0. dotnet core SDK 3.1 and runtime 3.1 and 2.1 
```
---- Sample SDK and RUNTIMES ----  
SDK : 3.1.301 [C:\Program Files\dotnet\sdk]
RUNTIMES :
Microsoft.AspNetCore.All 2.1.19 [C:\Program Files\dotnet\shared\Microsoft.AspNetCore.All]
Microsoft.AspNetCore.App 2.1.19 [C:\Program Files\dotnet\shared\Microsoft.AspNetCore.App]
Microsoft.AspNetCore.App 3.1.5 [C:\Program Files\dotnet\shared\Microsoft.AspNetCore.App]
Microsoft.NETCore.App 2.1.19 [C:\Program Files\dotnet\shared\Microsoft.NETCore.App]
Microsoft.NETCore.App 3.1.5 [C:\Program Files\dotnet\shared\Microsoft.NETCore.App]
Microsoft.WindowsDesktop.App 3.1.5 [C:\Program Files\dotnet\shared\Microsoft.WindowsDesktop.App]
```
### 1. git cli
### 2. docker desktop 
### 3. aws-vault

# Setup pantheon-server on local docker environment
# Setup pantheon-server on remote AWS environment   

## 0. Prerequisites


## 1. Check out code

Both `pantheon-server` and `with-buddies-server` must be checked out into their respective directories (with the same name) within the **same root folder** (in the example below, this is **pantheon**). This ideally results in the following folder structure:

* `drive:\path-to\pantheon\pantheon-server`
* `drive:\path-to\pantheon\with-buddies-server`

### 1.1 Tracking correct branches

Once the code is checked out into these two directories, ensure that:

* `pantheon-server` is tracking the `develop` branch
* `with-buddies-server` is tracking the `pantheon-develop` branch (**not** `develop`).

## 2. Obtain AWS credentials

Contact a Scopely administrator to obtain access keys for AWS so that a variety of remote tasks complete successfully in the next section and beyond. These are environment-specific so in the first instance you may receive a local dev key only.

### 2.1 Set up environment variables

The access keys can then be set up under Windows' system environment variables (Win + Pause > Advanced system settings > Advanced tab > Environment Variables button) with the names `WBAWSAccessKey` and `WBAWSSecretKey`.

## 3. Install Ruby and Rake

(Skip to 3.1 if Ruby and Rake are already installed.)

Install [Ruby for Windows](https://rubyinstaller.org) and then open a terminal window. At the command prompt enter `gem install rake` and hit Return. Rake should install.

### 3.1 Run the build tool

Once Rake is installed, enter the `pantheon-server` directory and run the rakefile as follows:

`rake configure env=local role=pantheon`

You may also need to perform this step while inside the `with-buddies-server` directory as well (though the first rake run may perform this additional step for you).

## 4. Restore .NET packages

Return to the `pantheon-server` folder at the command prompt and perform:

`dotnet restore --configfile ..\with-buddies-server\NuGet.config`

(Note: this **is** referencing a Nuget configuration file from a different working folder.)

### 4.1 Specifying the solution file

You may receive an error running the `dotnet restore` command above: "Specify which project or solution file to use because this folder contains more than one project or solution file." Fix this by appending the main solution file to the end of the above command, like so: 

`dotnet restore --configfile ..\with-buddies-server\NuGet.config Pantheon.Server.sln`

## 5. Building and Running
From this point the Pantheon.Server solution should build in Visual Studio. Unit tests must be run while connected to the MT London VPN due to IP address constraints.

### 5.1 Running the server locally

By default, `Pantheon.Server` is set to be the build target; to run the server locally, make `Pantheon.Server.Api` the build target, which will open Swagger in your browser when the project is run.