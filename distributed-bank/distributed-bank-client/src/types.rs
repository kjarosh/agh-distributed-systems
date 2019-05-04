use std::fmt;

use distributed_bank_client_if::bank::{AccountIdentification, AccountType};
use distributed_bank_client_if::bank::AccountType::{Standard, Premium};

pub struct AccountIdent {
    key: String,
    pesel: String,
    account_type: AccountType,
}

impl AccountIdent {
    pub fn new(pesel: String, key: String, account_type: AccountType) -> AccountIdent {
        AccountIdent { pesel, key, account_type }
    }

    pub fn to_identification(&self) -> AccountIdentification {
        AccountIdentification::new(self.pesel.clone(), self.key.clone())
    }

    pub fn is_premium(&self) -> bool {
        self.account_type == Premium
    }
}

impl fmt::Display for AccountIdent {
    fn fmt(&self, fmt: &mut fmt::Formatter) -> fmt::Result {
        fmt.write_str(&self.pesel)?;
        fmt.write_str(":")?;
        fmt.write_str(&self.key)?;
        fmt.write_str(":")?;
        fmt.write_str(if self.account_type == Standard { "STANDARD" } else { "PREMIUM" })?;

        Ok(())
    }
}


pub struct Money {
    value: i64,
}

impl Money {
    pub fn from_i64(value: i64) -> Money {
        Money {
            value
        }
    }
}

impl fmt::Display for Money {
    fn fmt(&self, fmt: &mut fmt::Formatter) -> fmt::Result {
        fmt.write_str(&(self.value / 100).to_string())?;
        fmt.write_str(".")?;
        fmt.write_str(&(self.value % 100).to_string())?;

        Ok(())
    }
}
