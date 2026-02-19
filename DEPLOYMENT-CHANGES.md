# Deployment Configuration Changes

## Summary of Changes

I've updated the CI/CD deployment configuration to meet your requirements:

1. **Changed deployment directory**:
   - Modified the GitHub Actions workflow to deploy to `/root/app` instead of `/opt/vend`
   - Updated all documentation references to use the new directory

2. **Added application cleanup**:
   - Added commands to kill the running application before deployment
   - Added a command to delete the old JAR file before copying the new one

3. **Added documentation**:
   - Created a new section explaining how to link GitHub Actions to your server
   - Updated all paths and commands in the documentation to reflect the new configuration

## How to Use the Updated Configuration

The updated CI/CD pipeline will now:

1. Build your application with Maven
2. Connect to your Digital Ocean server using SSH
3. Create the `/root/app` directory if it doesn't exist
4. Stop any running instance of the application
5. Delete the old JAR file
6. Copy the new JAR file to `/root/app/vend-0.0.1-SNAPSHOT.jar`
7. Start the application using `nohup` in the `/root/app` directory
8. Verify that the application is running

## Linking GitHub Actions to Your Server

To link GitHub Actions to your server, you need to:

1. **Set up SSH key authentication**:
   - Generate an SSH key pair if you don't have one
   - Add the public key to your server's `~/.ssh/authorized_keys` file
   - Add the private key as a GitHub secret named `SSH_PRIVATE_KEY`

2. **Set up known hosts verification**:
   - Run `ssh-keyscan -H your-server-ip` to get the server's SSH host keys
   - Add the output as a GitHub secret named `KNOWN_HOSTS`

3. **Set up connection details**:
   - Add your server's username (typically `root`) as a GitHub secret named `SSH_USERNAME`
   - Add your server's IP address as a GitHub secret named `SSH_HOST`

Detailed instructions for each of these steps are provided in the updated `.github/workflows/README.md` file.

## Checking Deployment Status

After pushing to the main branch, you can check if the deployment was successful by:

1. **Monitoring the GitHub Actions workflow**:
   - Go to the "Actions" tab in your GitHub repository
   - Click on the latest workflow run to see the details

2. **Checking if the application is running on your server**:
   ```bash
   ps aux | grep 'java -jar' | grep -v grep
   ```

3. **Checking the application logs**:
   ```bash
   cat /root/app/app4.log
   ```

## Manual Restart (if needed)

If you need to manually restart the application:

```bash
# Stop the current process
pkill -f 'java -jar /root/app/vend-0.0.1-SNAPSHOT.jar'

# Delete the old JAR file (optional)
rm -f /root/app/vend-0.0.1-SNAPSHOT.jar

# Copy the new JAR file (if you have it locally)
# scp local-path/vend-0.0.1-SNAPSHOT.jar root@your-server-ip:/root/app/

# Start the application
cd /root/app
nohup java -jar vend-0.0.1-SNAPSHOT.jar > app4.log 2>&1 &
```