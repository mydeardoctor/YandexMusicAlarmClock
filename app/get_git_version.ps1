# PowerShell script that gets the latest git tag from the current git branch
# and sets a global variable with the git tag.
# This script should be run as a prebuild step.

# Clears Version.kt contents.
$versionFilePath = "src/main/java/com/github/mydeardoctor/yandexmusicalarmclock/version/Version.kt"
"" | Set-Content $versionFilePath

# Adds package.
Add-Content $versionFilePath "package com.github.mydeardoctor.yandexmusicalarmclock.version`n`n"

# Gets the latest git tag from the current git branch.
$version = (git describe --tags --abbrev=0)
# Sets a global variable with the git tag.
Add-Content $versionFilePath "public const val VERSION: String = `"$version`""

# Writes a message to output indicating script completion.
Write-Host "get_git_version.ps1 finished"