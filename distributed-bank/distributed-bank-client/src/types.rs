use std::fmt;

use distributed_bank_client_if::bank::{AccountIdentification, AccountType};
use distributed_bank_client_if::bank::AccountType::{Premium, Standard};

pub struct AccountIdent {
    key: String,
    pesel: String,
    seqid: i64,
    account_type: AccountType,
}

impl AccountIdent {
    pub fn new(pesel: String, key: String, account_type: AccountType) -> AccountIdent {
        AccountIdent { pesel, key, account_type, seqid: 0 }
    }

    pub fn create_identification(&mut self) -> AccountIdentification {
        let seqid = self.next_seqid();
        let document = seqid.to_string() + ":" + self.key.as_ref();
        let signature = md5::compute(document).to_vec();
        AccountIdentification::new(self.pesel.clone(), signature, seqid)
    }

    pub fn is_premium(&self) -> bool {
        self.account_type == Premium
    }

    pub fn next_seqid(&mut self) -> i64 {
        self.seqid += 1;
        self.seqid
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
