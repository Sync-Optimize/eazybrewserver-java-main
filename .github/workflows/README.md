# CI/CD Setup Instructions for Digital Ocean Deployment

This document provides instructions on how to set up the CI/CD pipeline for deploying the Vend application to a Digital Ocean Ubuntu server.

## Prerequisites

1. A Digital Ocean account with an Ubuntu droplet created
2. SSH access to the Digital Ocean droplet
3. GitHub repository with the Vend application code
4. Java 17 installed on the Digital Ocean droplet

## Setting Up GitHub Secrets

The workflow uses several secrets that need to be configured in your GitHub repository. To add these secrets:

1. Go to your GitHub repository
2. Click on "Settings" > "Secrets and variables" > "Actions"
3. Click on "New repository secret"
4. Add the following secrets:

| Secret Name | Description | Example |
|-------------|-------------|---------|
| `SSH_PRIVATE_KEY` | The private SSH key to connect to your Digital Ocean droplet | `-----BEGIN OPENSSH PRIVATE KEY-----\n...\n-----END OPENSSH PRIVATE KEY-----` |
| `KNOWN_HOSTS` | The SSH known hosts entry for your Digital Ocean droplet | `123.456.789.0 ssh-rsa AAAAB3NzaC1yc2E...` |
| `SSH_USERNAME` | The username to use when connecting to your Digital Ocean droplet | `root` |
| `SSH_HOST` | The IP address or hostname of your Digital Ocean droplet | `123.456.789.0` |

### How to Generate These Values

#### SSH_PRIVATE_KEY
1. If you don't have an SSH key pair, generate one using:
   ```
   ssh-keygen -t rsa -b 4096 -C "your_email@example.com"
   ```
2. Copy the contents of the private key file (usually `~/.ssh/id_rsa`):
   ```
   cat ~/.ssh/id_rsa
   ```
3. Add the entire output (including the BEGIN and END lines) as the `SSH_PRIVATE_KEY` secret.

#### KNOWN_HOSTS
1. Get the known hosts entry for your Digital Ocean droplet (replace `123.456.789.0` with your actual droplet IP address):
   ```
   ssh-keyscan -H 123.456.789.0
   ```
2. Add the output as the `KNOWN_HOSTS` secret.

   **Important**: Make sure to replace `123.456.789.0` with your actual Digital Ocean droplet IP address. Using the placeholder text "your-droplet-ip" will result in a "Temporary failure in name resolution" error.

   **Troubleshooting**:
   - If you see an error like `getaddrinfo your-droplet-ip: Temporary failure in name resolution`, it means you need to use the actual IP address instead of the placeholder text.
   - If you're still having issues, you can manually create the KNOWN_HOSTS entry by using the format: `123.456.789.0 ssh-rsa AAAAB3NzaC1yc2E...` (replace with your actual IP and SSH key)
   - Alternatively, you can temporarily disable strict host key checking in the workflow file by modifying the SSH key action configuration.

#### SSH_USERNAME
This is typically `root` for Digital Ocean droplets, unless you've created a different user.

#### SSH_HOST
This is the IP address of your Digital Ocean droplet.

## Setting Up the Digital Ocean Droplet

1. Connect to your Digital Ocean droplet (replace `123.456.789.0` with your actual droplet IP address):
   ```
   ssh root@123.456.789.0
   ```

2. Install Java 17:
   ```
   apt update
   apt install -y openjdk-17-jdk
   ```

3. Verify Java installation:
   ```
   java -version
   ```

4. Create a directory for the application:
   ```
   mkdir -p /root/app
   ```

5. Make sure the SSH user has write permissions to the directory:
   ```
   chmod -R 755 /root/app
   ```

## Testing the Deployment

After setting up the secrets and the Digital Ocean droplet:

1. Push a change to the `main` branch of your repository
2. Go to the "Actions" tab in your GitHub repository to monitor the workflow
3. Once the workflow completes, check if the application is running on your Digital Ocean droplet:
   ```
   ps aux | grep 'java -jar' | grep -v grep
   ```

## Troubleshooting

If the deployment fails, check the following:

1. Verify that all secrets are correctly set in GitHub
2. Check if the SSH key has been added to the authorized_keys file on the Digital Ocean droplet
3. Ensure that Java 17 is correctly installed on the droplet
4. Check the logs of the application:
   ```
   cat /root/app/app4.log
   ```
5. If you need to manually restart the application:
   ```
   cd /root/app
   nohup java -jar vend-0.0.1-SNAPSHOT.jar > app4.log 2>&1 &
   ```

## Linking GitHub Actions to Your Server

The GitHub Actions workflow connects to your Digital Ocean server using SSH. Here's how the connection works:

1. **SSH Key Authentication**: The workflow uses the SSH private key (stored in the `SSH_PRIVATE_KEY` secret) to authenticate with your server.

2. **Known Hosts Verification**: The `KNOWN_HOSTS` secret is used to verify the identity of your server, preventing man-in-the-middle attacks.

3. **Connection Details**: The workflow uses the `SSH_USERNAME` and `SSH_HOST` secrets to determine which server to connect to and which user to authenticate as.

### How to Set Up the Connection

1. **Add your SSH public key to the server**: The public key corresponding to your `SSH_PRIVATE_KEY` must be added to the `~/.ssh/authorized_keys` file for the user specified in `SSH_USERNAME` on your server.

   ```bash
   # On your local machine, copy your public key
   cat ~/.ssh/id_rsa.pub

   # On your server, add it to authorized_keys
   echo "your-public-key-here" >> ~/.ssh/authorized_keys
   chmod 600 ~/.ssh/authorized_keys
   ```

2. **Verify SSH access**: Before running the GitHub Actions workflow, verify that you can SSH into your server using the private key:

   ```bash
   ssh -i /path/to/your/private/key root@your-server-ip
   ```

3. **Set up GitHub Secrets**: Add all the required secrets to your GitHub repository as described in the "Setting Up GitHub Secrets" section.

4. **Push to the main branch**: The workflow will automatically run when you push to the main branch, connecting to your server and deploying the application.

## Additional Configuration

### Database Setup

The application uses PostgreSQL. Make sure to:

1. Install PostgreSQL on your Digital Ocean droplet or use a managed database service
2. Configure the application's database connection properties in an `application.properties` file or through environment variables

### Environment Variables

You may need to set environment variables for the application. When using the nohup approach, you can set environment variables before running the application:

```bash
# Set environment variables
export SPRING_PROFILES_ACTIVE=prod
export DATABASE_URL=jdbc:postgresql://localhost:5432/venddb
export DATABASE_USERNAME=venduser
export DATABASE_PASSWORD=yourpassword

# Run the application with the environment variables
nohup java -jar vend-0.0.1-SNAPSHOT.jar > app4.log 2>&1 &
```

For a more permanent solution, you can add these environment variables to the deployment script in the GitHub Actions workflow file:

```yaml
# Start the application using nohup with environment variables
ssh ${{ secrets.SSH_USERNAME }}@${{ secrets.SSH_HOST }} "cd /root/app && export SPRING_PROFILES_ACTIVE=prod && export DATABASE_URL=jdbc:postgresql://localhost:5432/venddb && export DATABASE_USERNAME=venduser && export DATABASE_PASSWORD=yourpassword && nohup java -jar vend-0.0.1-SNAPSHOT.jar > app4.log 2>&1 &"
```

Alternatively, you can create a startup script on the server:

```bash
# Create a startup script
cat > /root/app/start.sh << 'EOL'
#!/bin/bash
export SPRING_PROFILES_ACTIVE=prod
export DATABASE_URL=jdbc:postgresql://localhost:5432/venddb
export DATABASE_USERNAME=venduser
export DATABASE_PASSWORD=yourpassword
cd /root/app
nohup java -jar vend-0.0.1-SNAPSHOT.jar > app4.log 2>&1 &
EOL

# Make it executable
chmod +x /root/app/start.sh

# Run the script
/root/app/start.sh
```
