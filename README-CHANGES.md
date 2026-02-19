# Changes to CI/CD Deployment Process

## Summary of Changes

I've updated the CI/CD deployment process to match how you're currently running the application on your server. Here's what I've changed:

1. **Modified the GitHub Actions workflow file** (.github/workflows/maven.yml):
   - Replaced the systemd service approach with the nohup command that you're currently using
   - Updated the JAR file name to match your current setup (vend-0.0.1-SNAPSHOT.jar)
   - Configured log redirection to app4.log as per your current setup
   - Added a verification step to check if the application is running after deployment

2. **Updated the documentation** (.github/workflows/README.md):
   - Updated the "Testing the Deployment" section to reflect the nohup approach
   - Updated the "Troubleshooting" section with commands relevant to the nohup approach
   - Updated the "Environment Variables" section with multiple options for setting environment variables with nohup

## How to Use the Updated CI/CD Setup

The CI/CD pipeline will now deploy your application using the same approach you're currently using manually:

```bash
nohup java -jar vend-0.0.1-SNAPSHOT.jar > app4.log 2>&1 &
```

### Key Benefits

1. **Automated Deployment**: Changes pushed to the main branch will automatically be deployed to your server
2. **Consistent Process**: The deployment process is now consistent with how you're currently running the application
3. **Familiar Logging**: Logs are still written to app4.log, making it easy to check for issues

### Checking Application Status

To check if the application is running:

```bash
ps aux | grep 'java -jar' | grep -v grep
```

To view the application logs:

```bash
cat /opt/vend/app4.log
```

### Manual Restart (if needed)

If you need to manually restart the application:

```bash
# Stop the current process
pkill -f 'java -jar /opt/vend/vend-0.0.1-SNAPSHOT.jar'

# Start the application again
cd /opt/vend
nohup java -jar vend-0.0.1-SNAPSHOT.jar > app4.log 2>&1 &
```

## Next Steps

1. Make sure your GitHub repository has the required secrets set up (SSH_PRIVATE_KEY, KNOWN_HOSTS, SSH_USERNAME, SSH_HOST)
2. Push a change to the main branch to trigger the CI/CD pipeline
3. Monitor the GitHub Actions workflow to ensure it completes successfully
4. Check if the application is running on your server using the commands provided above

For more detailed instructions, refer to the [CI/CD Setup Instructions](.github/workflows/README.md).