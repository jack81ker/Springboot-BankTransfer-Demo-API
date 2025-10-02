## SpringBoot - Wide Demo Rest API

### System Requirements
- Java 17 SDK
- Maven
- Docker
- MySQL
- Postman

&nbsp;

### Steps to Start and Run
1. go to _docker_ folder
2. make a copy of .env with sample (.env-example)
3. Open your command prompt and type following command
    ```
        shell> docker compose up -d
   ```
4. please wait until all the container starter up, and then you are ready to go.

&nbsp;

### API Documentation

Please refer to Swagger: http://localhost:8080/swagger-ui.html

&nbsp;

### Test Data
Admin can only access following API on-behalf of customer
1. List customer transfer history
2. List customer bank accounts

Admin
```
Credentials
username: admin
password: admin321
```

User 1
```
Credentials
username: user1
password: user123

Bank Account Details
Account Number: 11401201221
Account Type: Savings
Currency: IDR
Account Balance: 74.5000
````

User 2
```
Credentials
username: user3
password: user123

Bank Account Details
Account Number: 34555512333
Account Type: Savings
Currency: IDR
Account Balance: 25.5000
```

### To start testing in the Postman
1. Import collections and environments in the _postman_ folder to your postman.
2. In your postman, navigate to Collections -> authentication -> Login
3. Authenticate yourself with the credentials provided above. The jwt token in the response is stored in the Environment.


## Database Table explanations
1. bank_accounts - Customer bank accounts
2. ledger - Customer bank account balance

    **Important**: user1 preloaded with 100 IDR.

3. transaction_setting - System wide Transaction Setting
    e.g: a customer can only transfer not more than 50,000,000 IDR in a DAY.
    
    **IMPORTANT**: make sure this table is not empty.

4. transfer - the actual transfer records
5. transfer_log - the audit log of every transfer attempt(pending/success/failure)
6. users - the principal table containing username and encoded user password.