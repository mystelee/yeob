package yeobchain;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;

public class YeobChain {

	public static ArrayList<Block> blockchain = new ArrayList<Block>();
	public static HashMap<String, TransactionOutput> UTXOs = new HashMap<String, TransactionOutput>();

	public static int difficulty = 3;
	public static float minimumTransaction = 0.1f;
	public static Wallet walletA;
	public static Wallet walletB;
	public static Transaction genesisTransaction;

	public static void main(String[] args) {

		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

		// 지갑생성
		walletA = new Wallet();
		walletB = new Wallet();
		Wallet coinbase = new Wallet();

		// 최초 거래, 100개 엽코인 A지갑에
		genesisTransaction = new Transaction(coinbase.publicKey, walletA.publicKey, 100f, null);
		genesisTransaction.generateSignature(coinbase.privateKey); // 수동으로 제네시스 트랜잭션 서명
		genesisTransaction.transactionId = "0"; // 수동으로 트랜잭션 아이디 셋팅
		genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.reciepient, genesisTransaction.value,
				genesisTransaction.transactionId)); // 수동으로 틀랜잭션 출력값 추가
		UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0)); // 중요, 첫번째 트랜잭션을 UTXO 리스트에
																							// 추가

		System.out.println("Creating and Mining Genesis block...");
		Block genesis = new Block("0");
		genesis.addTransaction(genesisTransaction);
		addBlock(genesis);

		// test
		Block block1 = new Block(genesis.hash);
		System.out.println("\nWalletA's balance is: " + walletA.getBalance());
		System.out.println("\nWalletA is Attempting to send funds (40) to WalletB...");
		block1.addTransaction(walletA.sendFunds(walletB.publicKey, 40f));
		addBlock(block1);
		System.out.println("\nWalletA's balance is: " + walletA.getBalance());
		System.out.println("WalletB's balance is: " + walletB.getBalance());

		Block block2 = new Block(block1.hash);
		System.out.println("\nWalletA Attempting to send more funds (1000) than it has...");
		block2.addTransaction(walletA.sendFunds(walletB.publicKey, 1000f));
		addBlock(block2);
		System.out.println("\nWalletA's balance is: " + walletA.getBalance());
		System.out.println("WalletB's balance is: " + walletB.getBalance());

		isChainValid();

		/*
		 * // 개인 키 및 공개 키 테스트 System.out.println("Private and public keys:");
		 * System.out.println(StringUtil.getStringFromKey(walletA.privateKey));
		 * System.out.println(StringUtil.getStringFromKey(walletA.publicKey));
		 * 
		 * // test public and private keys
		 * System.out.println("Private and public keys:");
		 * System.out.println(StringUtil.getStringFromKey(walletA.privateKey));
		 * System.out.println(StringUtil.getStringFromKey(walletA.publicKey));
		 * 
		 * // A->B 로 테스트 거래 생성 Transaction transaction = new
		 * Transaction(walletA.publicKey, walletB.publicKey, 5, null);
		 * transaction.generateSignature(walletA.privateKey);
		 * 
		 * System.out.println("Is signature verified");
		 * System.out.println(transaction.verifiySignature());
		 */

		/*
		 * blockchain.add(new Block("Hi im the first block", "0"));
		 * System.out.println("Trying to Mine block 1...");
		 * blockchain.get(0).mineBlock(difficulty);
		 * 
		 * blockchain.add(new Block("Yo im the second block",
		 * blockchain.get(blockchain.size() - 1).hash));
		 * System.out.println("Trying to Mine block 2...");
		 * blockchain.get(1).mineBlock(difficulty);
		 * 
		 * blockchain.add(new Block("Hey im the third block",
		 * blockchain.get(blockchain.size() - 1).hash));
		 * System.out.println("Trying to Mine block 3...");
		 * blockchain.get(2).mineBlock(difficulty);
		 * 
		 * System.out.println("\nBlockchain is Valid : " + isChainValid());
		 * 
		 * String blockchainJson = new
		 * GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
		 * System.out.println("\nThe block chain : ");
		 * System.out.println(blockchainJson);
		 */

		/*
		 * Block genesisBlock = new Block("Hi im the first block", "0");
		 * System.out.println("Hash for block 1 : " + genesisBlock.hash);
		 * 
		 * Block secondBlock = new Block("Yo im the second block", genesisBlock.hash);
		 * System.out.println("Hash for block 2 : " + secondBlock.hash);
		 * 
		 * Block thirdBlock = new Block("Hey im the third block", secondBlock.hash);
		 * System.out.println("Hash for block 3 : " + thirdBlock.hash);
		 */
	}// end main

	public static Boolean isChainValid() {
		Block currentBlock;
		Block previousBlock;
		String hashTarget = new String(new char[difficulty]).replace('\0', '0');
		HashMap<String, TransactionOutput> tempUTXOs = new HashMap<String, TransactionOutput>(); // 지정된 블록상태에서 사용하지 않는
																									// 트랙잭션의 임시 목록
		tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

		// 해시 검사를 위한 반복문
		for (int i = 1; i < blockchain.size(); i++) {

			currentBlock = blockchain.get(i);
			previousBlock = blockchain.get(i - 1);

			// 등록된 해시와 계산된 해시 비교
			if (!currentBlock.hash.equals(currentBlock.calculateHash())) {
				System.out.println("Current Hashes not equal");
				return false;
			}
			// 이전 해시와 등록된 이전 해시값 비교
			if (!previousBlock.hash.equals(currentBlock.previousHash)) {
				System.out.println("Previous Hashes not equal");
				return false;
			}
			// 완료된 해시 비교
			if (!currentBlock.hash.substring(0, difficulty).equals(hashTarget)) {
				System.out.println("This block hasn't been mined");
				return false;
			}

			// 블록체인 트랜잭션을 통한 루프
			TransactionOutput tempOutput;
			for (int t = 0; t < currentBlock.transactions.size(); t++) {
				Transaction currentTransaction = currentBlock.transactions.get(t);

				if (!currentTransaction.verifiySignature()) {
					System.out.println("#Signature on Transaction(" + t + ") is Invalid");
					return false;
				}
				if (currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
					System.out.println("#Inputs are note equal to outputs on Transaction(" + t + ")");
					return false;
				}

				for (TransactionInput input : currentTransaction.inputs) {
					tempOutput = tempUTXOs.get(input.transactionOutputId);

					if (tempOutput == null) {
						System.out.println("#Referenced input on Transaction(" + t + ") is Missing");
						return false;
					}

					if (input.UTXO.value != tempOutput.value) {
						System.out.println("#Referenced input Transaction(" + t + ") value is Invalid");
					}

					tempUTXOs.remove(input.transactionOutputId);
				}

				for (TransactionOutput output : currentTransaction.outputs) {
					tempUTXOs.put(output.id, output);
				}

				if (currentTransaction.outputs.get(0).reciepient != currentTransaction.reciepient) {
					System.out.println("#Transaction(\" + t + \") output reciepient is not who it should be");
					return false;
				}
				if (currentTransaction.outputs.get(1).reciepient != currentTransaction.sender) {
					System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
					return false;
				}
			}

		}
		System.out.println("Blockchain is valid");
		return true;
	}

	public static void addBlock(Block newBlock) {
		newBlock.mineBlock(difficulty);
		blockchain.add(newBlock);
	}

}// end YeobChain
