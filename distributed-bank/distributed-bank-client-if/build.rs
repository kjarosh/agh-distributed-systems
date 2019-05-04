use std::fs::create_dir_all;
use std::process::{Command, exit};
use std::fs;

fn main() {
    let generated_path = "./target/generated";
    create_dir_all(generated_path).expect("Cannot create directory");
    let exit_status = Command::new("thrift")
        .arg("-out")
        .arg(generated_path)
        .arg("--gen")
        .arg("rs")
        .arg("-r")
        .arg("../distributed-bank-core-if/src/main/thrift/bank.thrift")
        .status()
        .expect("Exit status expected")
        .code()
        .expect("Exit code expected");

    fs::copy("src/lib.rs", "target/generated/lib.rs")
        .expect("Cannot copy files");

    exit(exit_status);
}
