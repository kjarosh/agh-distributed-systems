use std::io;

use thrift::protocol::*;
use thrift::transport::*;

use distributed_bank_client_if::bank::*;

pub fn read_line() -> String {
    let mut input = String::new();
    io::stdin().read_line(&mut input).unwrap();
    return String::from(input.trim());
}

pub fn read_price() -> i64 {
    let input = read_line();
    if input.contains(".") {
        input.replace(".", "").parse::<i64>().unwrap()
    } else {
        input.parse::<i64>().unwrap() * 100
    }
}

type ClientInputProtocol = TBinaryInputProtocol<TBufferedReadTransport<ReadHalf<TTcpChannel>>>;
type ClientOutputProtocol = TBinaryOutputProtocol<TBufferedWriteTransport<WriteHalf<TTcpChannel>>>;
pub type AccountManagementClient = AccountManagementSyncClient<ClientInputProtocol, ClientOutputProtocol>;
pub type StandardAccountClient = StandardAccountSyncClient<ClientInputProtocol, ClientOutputProtocol>;
pub type PremiumAccountClient = PremiumAccountSyncClient<ClientInputProtocol, ClientOutputProtocol>;

pub fn new_account_management_client(
    host: &str,
    port: u16,
) -> thrift::Result<AccountManagementClient> {
    let (i_prot, o_prot) = new_connection(host, port)?;

    Ok(AccountManagementSyncClient::new(i_prot, o_prot))
}

pub fn new_standard_account_client(
    host: &str,
    port: u16,
) -> thrift::Result<StandardAccountClient> {
    let (i_prot, o_prot) = new_connection(host, port)?;

    Ok(StandardAccountSyncClient::new(i_prot, o_prot))
}

pub fn new_premium_account_client(
    host: &str,
    port: u16,
) -> thrift::Result<PremiumAccountClient> {
    let (i_prot, o_prot) = new_connection(host, port)?;

    Ok(PremiumAccountSyncClient::new(i_prot, o_prot))
}

fn new_connection(host: &str, port: u16) -> thrift::Result<(ClientInputProtocol, ClientOutputProtocol)> {
    let mut channel = TTcpChannel::new();
    println!("connecting to server on {}:{}", host, port);
    channel.open(&format!("{}:{}", host, port))?;
    let (i_chan, o_chan) = channel.split()?;
    let i_tran = TBufferedReadTransport::new(i_chan);
    let o_tran = TBufferedWriteTransport::new(o_chan);
    let i_prot = TBinaryInputProtocol::new(i_tran, false);
    let o_prot = TBinaryOutputProtocol::new(o_tran, false);
    Ok((i_prot, o_prot))
}
