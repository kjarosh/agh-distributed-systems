
namespace java pl.edu.agh.student.kjarosz.distributedsystems.bank.api
namespace py bank_api.bank

// for every price with type i64
//   1234 = 12.34 PLN

enum AccountType {
    STANDARD,
    PREMIUM,
}

struct AccountIdentification {
    1: string pesel,
    2: string key,
}

struct AccountCreationRequest {
    1: string firstName,
    2: string lastName,
    3: string pesel,
    4: i64 monthlySalary,
}

struct AccountCreationResponse {
    1: string key,
    2: AccountType type,
}

struct LoanRequest {
    1: string currency,
    2: i64 value,
    // duration in days
    3: i32 duration,
}

struct LoanAcknowledgement {
    1: i64 price,
    2: i64 foreignPrice,
    3: double exchangeRate,
}

exception InvalidAccount {
    1: string message,
}

exception InvalidCurrency {
    1: string currency,
    2: string message,
}

service AccountManagement {
    AccountCreationResponse createAccount(
        1: AccountCreationRequest request),
}

service StandardAccount {
    i64 accountBalance(
        1: AccountIdentification accountIdent)
    throws (1: InvalidAccount ex),
}

service PremiumAccount extends StandardAccount {
    LoanAcknowledgement takeLoan(
        1: AccountIdentification accountIdent,
        2: LoanRequest loanRequest)
    throws (
        1: InvalidAccount e1,
        2: InvalidCurrency e2),
}
