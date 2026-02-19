# GitHub Advanced Security Setup Guide

## What is GitHub Advanced Security?

GitHub Advanced Security (GHAS) is a suite of security features that help you identify and fix vulnerabilities in your code. It includes:

1. **Dependency Review**: Automatically review dependencies for vulnerabilities when a pull request is opened
2. **Secret Scanning**: Detect secrets (like API keys, tokens) accidentally committed to your repository
3. **Code Scanning**: Identify security vulnerabilities and coding errors in your code using CodeQL
4. **Dependabot**: Automatically update vulnerable dependencies and create security alerts

## Prerequisites

GitHub Advanced Security is available for:
- GitHub Enterprise Cloud (all repositories)
- GitHub Enterprise Server (version 3.0 or higher)
- Public repositories on GitHub.com (free)
- Private repositories (requires a license)

## How to Activate GitHub Advanced Security

### For Public Repositories

For public repositories, GitHub Advanced Security features are automatically available. You don't need to explicitly enable them, but you may need to configure specific features.

### For Private Repositories

To activate GitHub Advanced Security for a private repository:

1. Go to your GitHub repository
2. Click on "Settings" in the top navigation bar
3. In the left sidebar, click on "Security & analysis"
4. Find "GitHub Advanced Security" and click "Enable"
5. Review the information about additional data that will be sent to GitHub
6. Click "Enable GitHub Advanced Security for this repository" to confirm

## Configuring Specific Security Features

### 1. Dependency Review

Dependency review is automatically enabled when you enable GitHub Advanced Security. It will:
- Scan your dependencies when a pull request is opened
- Show vulnerabilities in the pull request diff view

### 2. Secret Scanning

To enable Secret Scanning:

1. Go to your repository's "Settings"
2. Click on "Security & analysis" in the left sidebar
3. Find "Secret scanning" and click "Enable"

### 3. Code Scanning

To set up Code Scanning with CodeQL:

1. Go to your repository's "Security" tab
2. Click on "Code scanning" in the left sidebar
3. Click "Set up code scanning"
4. Choose "CodeQL Analysis" from the list of workflows
5. Click "Set up this workflow"

This will create a new workflow file in `.github/workflows/codeql-analysis.yml`. You can customize this file to fit your needs.

Example CodeQL workflow file:
```yaml
name: "CodeQL"

on:
  push:
    branches: [ "main" ]
  pull_request:
    branches: [ "main" ]
  schedule:
    - cron: '30 1 * * 0'

jobs:
  analyze:
    name: Analyze
    runs-on: ubuntu-latest
    permissions:
      actions: read
      contents: read
      security-events: write

    strategy:
      fail-fast: false
      matrix:
        language: [ 'java' ]

    steps:
    - name: Checkout repository
      uses: actions/checkout@v4

    - name: Initialize CodeQL
      uses: github/codeql-action/init@v3
      with:
        languages: ${{ matrix.language }}

    - name: Autobuild
      uses: github/codeql-action/autobuild@v3

    - name: Perform CodeQL Analysis
      uses: github/codeql-action/analyze@v3
      with:
        category: "/language:${{matrix.language}}"
```

### 4. Dependabot

To enable Dependabot alerts and security updates:

1. Go to your repository's "Settings"
2. Click on "Security & analysis" in the left sidebar
3. Find "Dependabot alerts" and click "Enable"
4. Find "Dependabot security updates" and click "Enable"

To configure Dependabot, create a `.github/dependabot.yml` file:

```yaml
version: 2
updates:
  - package-ecosystem: "maven"
    directory: "/"
    schedule:
      interval: "weekly"
    open-pull-requests-limit: 10
```

## Best Practices

1. **Review Security Alerts Regularly**: Check the "Security" tab of your repository frequently
2. **Set Up Notifications**: Configure notifications for security alerts
3. **Automate Security Fixes**: Enable Dependabot security updates to automatically create PRs for vulnerable dependencies
4. **Include Security Checks in CI/CD**: Make security scanning part of your CI/CD pipeline
5. **Document Security Policies**: Create a SECURITY.md file in your repository

## Monitoring Security Status

1. Go to your repository's "Security" tab
2. Review the security overview, which includes:
   - Dependabot alerts
   - Code scanning alerts
   - Secret scanning alerts
3. Click on specific alert types to see detailed information

## Conclusion

GitHub Advanced Security provides powerful tools to help secure your codebase. By enabling and properly configuring these features, you can automatically detect and fix security vulnerabilities, preventing them from reaching production.

For your Vend application, enabling GitHub Advanced Security will help ensure that your dependencies are secure, your code doesn't contain vulnerabilities, and no secrets are accidentally exposed in your repository.
