# GitHub Actions Setup Guide

## How GitHub Actions Picks Up Your Workflow

GitHub Actions automatically detects and runs workflow files stored in the `.github/workflows` directory of your repository. You don't need to manually register or activate these workflows - GitHub automatically recognizes them when:

1. You push the workflow file to your repository
2. A trigger event specified in the workflow file occurs (in your case, a push to the `main` branch)

Your `maven.yml` workflow file is already correctly placed in the `.github/workflows` directory, so GitHub will automatically detect and run it when you push to the `main` branch.

## Do You Need to Set Up a Runner?

**No, you don't need to set up your own runner.** Your workflow is configured to use GitHub-hosted runners:

```yaml
jobs:
  build:
    runs-on: ubuntu-latest
  
  deploy:
    runs-on: ubuntu-latest
```

The `runs-on: ubuntu-latest` line tells GitHub to run your workflow on GitHub's own Ubuntu runners. These are virtual machines that GitHub provides and maintains for you. You don't need to:
- Set up any infrastructure
- Install any software
- Manage any virtual machines

GitHub takes care of all of this automatically.

## What Dependencies Do You Need to Set Up?

The only things you need to set up on GitHub's side are:

1. **GitHub Secrets**: You need to add the following secrets to your repository:
   - `SSH_PRIVATE_KEY`: Your SSH private key for connecting to your Digital Ocean server
   - `KNOWN_HOSTS`: The SSH known hosts entry for your Digital Ocean server
   - `SSH_USERNAME`: The username to use when connecting to your Digital Ocean server (typically `root`)
   - `SSH_HOST`: The IP address of your Digital Ocean server

2. **Repository Permissions**: If you're using the dependency graph feature, you need to ensure GitHub Advanced Security features are enabled for your repository.

That's it! You don't need to:
- Install Java or Maven on GitHub's side (the workflow does this automatically)
- Set up any build servers or CI/CD infrastructure
- Install any plugins or extensions to GitHub

## Step-by-Step Setup Instructions

### 1. Add GitHub Secrets

1. Go to your GitHub repository
2. Click on "Settings" > "Secrets and variables" > "Actions"
3. Click on "New repository secret"
4. Add each of the following secrets:

   a. **SSH_PRIVATE_KEY**
   - Name: `SSH_PRIVATE_KEY`
   - Value: Your SSH private key (the entire content including BEGIN and END lines)
   
   b. **KNOWN_HOSTS**
   - Name: `KNOWN_HOSTS`
   - Value: The output from `ssh-keyscan -H your-server-ip` (which you've already generated)
   
   c. **SSH_USERNAME**
   - Name: `SSH_USERNAME`
   - Value: `root` (or whatever username you use to connect to your server)
   
   d. **SSH_HOST**
   - Name: `SSH_HOST`
   - Value: Your Digital Ocean server's IP address (e.g., `159.65.181.236`)

### 2. Ensure Your Repository Has the Workflow File

The `.github/workflows/maven.yml` file should already be in your repository. If it's not, you need to:

1. Create the `.github/workflows` directory if it doesn't exist
2. Add the `maven.yml` file with the content you've already configured

### 3. Push to the Main Branch

Once you've set up the secrets and ensured the workflow file is in your repository, simply push to the `main` branch to trigger the workflow:

```bash
git add .
git commit -m "Update configuration"
git push origin main
```

### 4. Monitor the Workflow

1. Go to your GitHub repository
2. Click on the "Actions" tab
3. You should see your workflow running (or completed if it's already finished)
4. Click on the workflow run to see the details and logs

## What Happens When You Push to Main

Here's what happens automatically when you push to the `main` branch:

1. GitHub detects the push event
2. GitHub sees that your workflow is configured to run on push to `main`
3. GitHub starts a new virtual machine with Ubuntu
4. GitHub checks out your code
5. The workflow installs Java 17 and Maven
6. The workflow builds your application
7. If the build is successful, the workflow deploys your application to your Digital Ocean server using SSH

## Troubleshooting

If your workflow fails, check the following:

1. **Secrets**: Ensure all secrets are correctly set up
2. **SSH Access**: Verify that the SSH key has access to your server
3. **Workflow File**: Check for any syntax errors in your workflow file
4. **Build Issues**: Look for any build errors in the workflow logs

## Conclusion

GitHub Actions is a fully managed CI/CD service. You don't need to set up any runners, build servers, or other infrastructure. GitHub provides all of this for you. The only things you need to configure are the workflow file (which you've already done) and the secrets needed for deployment.

Your current setup is already correctly configured to use GitHub's infrastructure. Once you've added the required secrets, your CI/CD pipeline should work automatically whenever you push to the `main` branch.