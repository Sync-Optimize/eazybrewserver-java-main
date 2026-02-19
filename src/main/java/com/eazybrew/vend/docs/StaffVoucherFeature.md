# Staff Voucher Feature

## Overview

The Staff Voucher feature allows companies to provide their staff with daily spending limits on a monthly basis. Staff members can use their NFC cards to make purchases, and the system will automatically deduct the purchase amount from their voucher balance if they have one.

## Key Concepts

### Voucher Properties

- **Daily Limit**: Maximum amount a staff member can spend per day
- **Total Amount**: Total remaining amount in the voucher
- **Used Amount Today**: Amount used today (resets daily)
- **Last Reset Date**: Date when the used amount was last reset
- **Start Date**: Start date of the voucher validity
- **End Date**: End date of the voucher validity
- **Enabled**: Flag to enable/disable the voucher

### Voucher Spending Rules

1. When a staff member taps their NFC card to make a purchase:
   - The system checks if they have a valid voucher
   - If the voucher has expired (outside start-end date range), the purchase is declined
   - If the requested amount exceeds the daily limit, the purchase is declined
   - If the requested amount exceeds the total remaining amount, the purchase is declined
   - Otherwise, the amount is deducted from the voucher balance

2. Daily Reset:
   - The system automatically resets the "Used Amount Today" to zero when a new day starts
   - This allows staff members to spend up to their daily limit each day

## API Endpoints

### Voucher Management (Admin Only)

- **Create Voucher**: `POST /api/vouchers/companies/{companyId}`
  - Creates a new voucher for a staff member
  - Requires ADMIN or SUPERADMIN role

- **Fund Voucher**: `POST /api/vouchers/companies/{companyId}/fund`
  - Adds funds to an existing voucher or creates a new one if it doesn't exist
  - Requires ADMIN or SUPERADMIN role

- **Get Voucher by Staff**: `GET /api/vouchers/companies/{companyId}/staff/{staffId}`
  - Retrieves voucher information for a specific staff member
  - Requires ADMIN or SUPERADMIN role

- **Get Vouchers by Company**: `GET /api/vouchers/companies/{companyId}`
  - Retrieves all vouchers for a company
  - Requires ADMIN or SUPERADMIN role

- **Disable Voucher**: `PUT /api/vouchers/companies/{companyId}/staff/{staffId}/disable`
  - Disables a voucher for a staff member
  - Requires ADMIN or SUPERADMIN role

- **Enable Voucher**: `PUT /api/vouchers/companies/{companyId}/staff/{staffId}/enable`
  - Enables a voucher for a staff member
  - Requires ADMIN or SUPERADMIN role

### Transaction Processing (Automatic)

The voucher check is automatically integrated into the transaction processing flow. When a staff member makes a purchase using their NFC card, the system:

1. Checks if they have a valid voucher
2. Verifies if they can spend the requested amount
3. If approved, deducts the amount from their voucher
4. If not approved or no voucher exists, proceeds with regular payment processing

## Example Usage

### Creating a Voucher

```
POST /api/vouchers/companies/1
```

Request body:
```json
{
  "staffId": 123,
  "dailyLimit": 2000.00,
  "totalAmount": 60000.00,
  "startDate": "2023-01-01",
  "endDate": "2023-01-31"
}
```

### Funding a Voucher

```
POST /api/vouchers/companies/1/fund
```

Request body:
```json
{
  "staffId": 123,
  "amount": 10000.00
}
```

## Implementation Details

The voucher system is implemented with the following components:

1. **Voucher Model**: Represents a staff voucher with all necessary properties
2. **VoucherService**: Handles business logic for voucher operations
3. **VoucherController**: Provides REST API endpoints for voucher management
4. **TransactionService Integration**: Checks and uses vouchers during transaction processing

The system is designed to be robust, handling edge cases such as:
- Daily limit resets
- Voucher expiration
- Insufficient funds
- Error handling during voucher operations
