# SSH-KEYSCAN Issue Fix

## Issue Description

You encountered the following error when trying to generate the KNOWN_HOSTS entry for your GitHub Actions workflow:

```
ssh-keyscan -H your-droplet-ip
getaddrinfo your-droplet-ip: Temporary failure in name resolution
getaddrinfo your-droplet-ip: Temporary failure in name resolution
getaddrinfo your-droplet-ip: Temporary failure in name resolution
getaddrinfo your-droplet-ip: Temporary failure in name resolution
getaddrinfo your-droplet-ip: Temporary failure in name resolution
```

## Root Cause

The error occurred because the command was run with the placeholder text "your-droplet-ip" instead of replacing it with your actual Digital Ocean droplet's IP address.

## Solution

I've updated the CI/CD documentation (.github/workflows/README.md) to:

1. Replace all instances of "your-droplet-ip" with a more obvious example IP address "123.456.789.0"
2. Add clear instructions to replace the example IP with your actual droplet IP address
3. Add troubleshooting information specifically addressing the "Temporary failure in name resolution" error
4. Provide alternative approaches if ssh-keyscan continues to fail

## How to Generate the KNOWN_HOSTS Entry Correctly

To generate the KNOWN_HOSTS entry correctly, follow these steps:

1. Replace `123.456.789.0` with your actual Digital Ocean droplet IP address in the following command:
   ```
   ssh-keyscan -H 123.456.789.0
   ```

2. Run the command and copy the output

3. Add the output as the `KNOWN_HOSTS` secret in your GitHub repository

## Alternative Approaches

If you continue to have issues with the ssh-keyscan command, you can:

1. Manually create the KNOWN_HOSTS entry by using the format: 
   ```
   123.456.789.0 ssh-rsa AAAAB3NzaC1yc2E...
   ```
   (replace with your actual IP and SSH key)

2. Temporarily disable strict host key checking in the workflow file by modifying the SSH key action configuration in your GitHub Actions workflow file (.github/workflows/maven.yml):

   ```yaml
   - name: Install SSH key
     uses: shimataro/ssh-key-action@v2
     with:
       key: ${{ secrets.SSH_PRIVATE_KEY }}
       known_hosts: ${{ secrets.KNOWN_HOSTS }}
       config: |
         Host *
           StrictHostKeyChecking no
   ```

   Note: Disabling strict host key checking reduces security and should only be used as a temporary solution.

## Next Steps

1. Try generating the KNOWN_HOSTS entry again using your actual droplet IP address
2. Update the GitHub secret with the correct value
3. Push a change to the main branch to trigger the CI/CD pipeline
4. Monitor the GitHub Actions workflow to ensure it completes successfully

For more detailed instructions, refer to the updated [CI/CD Setup Instructions](.github/workflows/README.md).