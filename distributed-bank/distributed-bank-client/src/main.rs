extern crate clap;
extern crate distributed_bank_client_if;
extern crate md5;
extern crate thrift;
extern crate try_from;

use clap::*;

use distributed_bank_client_if::bank::*;
use types::*;
use utils::*;

mod utils;
mod types;

fn main() {
    match run() {
        Ok(()) => {}
        Err(e) => {
            println!("error {:?}", e);
            std::process::exit(1);
        }
    }
}

fn run() -> thrift::Result<()> {
    let options = clap_app!(rust_tutorial_client =>
        (version: "0.1.0")
        (author: "Kamil Jarosz <kjarosz@student.agh.edu.pl>")
        (about: "Bank client app")
        (@arg host: --host +takes_value "host")
        (@arg mport: --mport +takes_value "account management port")
        (@arg sport: --sport +takes_value "account services port")
    );
    let matches = options.get_matches();

    let host = matches.value_of("host").unwrap_or("127.0.0.1");
    let management_port = value_t!(matches, "mport", u16).unwrap_or(9091);
    let services_port = value_t!(matches, "sport", u16).unwrap_or(9092);

    let account_management_client = &mut new_account_management_client(host, management_port)?;

    let mut accounts: Vec<AccountIdent> = Vec::new();

    loop {
        let account_ix = select_account(&mut accounts, account_management_client)?;
        let ident = &mut accounts[account_ix];
        println!("ident: {}", ident);

        if ident.is_premium() {
            run_premium(host, services_port, ident)?
        } else {
            run_standard(host, services_port, ident)?
        }
    }

    Ok(())
}

fn run_standard(host: &str, services_port: u16, ident: &mut AccountIdent) -> thrift::Result<()> {
    let standard_account_client = &mut new_standard_account_client(host, services_port)?;
    loop {
        println!(">>> what do you want to do?");
        let input = read_line();
        match input.as_ref() {
            "balance" => {
                println!("your balance:");
                println!("{}", get_balance_standard(standard_account_client, ident)?)
            }
            "loan" => println!("broke eh? sorry, cannot help"),
            "switch" => break,
            "exit" => std::process::exit(0),
            _ => println!("unknown command"),
        }
    }

    Ok(())
}

fn run_premium(host: &str, services_port: u16, ident: &mut AccountIdent) -> thrift::Result<()> {
    let premium_account_client = &mut new_premium_account_client(host, services_port)?;
    loop {
        println!(">>> what do you want to do?");
        let input = read_line();
        match input.as_ref() {
            "balance" => {
                println!("your balance:");
                println!("{}", get_balance_premium(premium_account_client, ident)?)
            }
            "loan" => {
                if !ident.is_premium() {
                    println!("broke eh? sorry, cannot help");
                    continue;
                }

                println!(">>> currency?");
                let currency = read_line();
                println!(">>> loan value?");
                let value = read_price();
                println!(">>> loan duration in days?");
                let duration = read_line().parse::<i32>().unwrap();
                match take_loan(premium_account_client, ident, currency, value, duration) {
                    Ok(ack) => {
                        println!("exchange rate: {}", ack.exchange_rate.unwrap());
                        println!("price: {}", Money::from_i64(ack.price.unwrap()));
                        println!("foreign price: {}", Money::from_i64(ack.foreign_price.unwrap()))
                    },
                    Err(e) => {
                        println!("error {:?}", e);
                    }
                }
            }
            "switch" => break,
            "exit" => return Ok(()),
            _ => println!("unknown command"),
        }
    }

    Ok(())
}

fn get_balance_standard(client: &mut StandardAccountClient, ident: &mut AccountIdent) -> thrift::Result<Money> {
    let response =
        client.account_balance(ident.create_identification())?;
    return Ok(Money::from_i64(response));
}

fn get_balance_premium(client: &mut PremiumAccountClient, ident: &mut AccountIdent) -> thrift::Result<Money> {
    let response =
        client.account_balance(ident.create_identification())?;
    return Ok(Money::from_i64(response));
}

fn take_loan(
    client: &mut PremiumAccountClient,
    ident: &mut AccountIdent,
    currency: String,
    value: i64,
    duration: i32,
) -> thrift::Result<LoanAcknowledgement> {
    let request = LoanRequest::new(currency, value, duration);
    let response =
        client.take_loan(ident.create_identification(), request)?;
    return Ok(response);
}

fn select_account(
    accounts: &mut Vec<AccountIdent>,
    client: &mut AccountManagementClient
) -> thrift::Result<usize> {
    println!("Options:");
    println!("1) New Account");
    let mut i = 2;
    for acc in accounts.iter_mut() {
        println!("{}) {}", i, acc);
        i += 1;
    }

    loop {
        match read_line().parse::<usize>() {
            Ok(val) => {
                if val == 1 {
                    let account = create_account(client)?;
                    accounts.push(account);
                    return Ok(accounts.len() - 1);
                }

                return Ok(val - 2);
            }
            Err(_) => continue
        }
    }
}

fn create_account(client: &mut AccountManagementClient) -> thrift::Result<AccountIdent> {
    println!("first name: ", );
    let first_name = read_line();
    println!("last name: ");
    let last_name = read_line();
    println!("pesel: ");
    let pesel = read_line();
    println!("monthly salary: ");
    let salary = read_price();
    let request = AccountCreationRequest::new(
        first_name.to_owned(),
        last_name.to_owned(),
        pesel.to_owned(),
        salary);
    let response =
        client.create_account(request)?;
    return Ok(AccountIdent::new(
        first_name.to_owned(),
        last_name.to_owned(),
        pesel.to_owned(),
        response.key.unwrap(),
        response.type_.unwrap()));
}
