package yeobchain;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

public class Transaction {

	public String transactionId;
	public PublicKey sender;
	public PublicKey reciepient;
	public float value;
	public byte[] signature;

	public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
	public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();

	private static int sequence = 0;

	// Constructor:
	public Transaction(PublicKey from, PublicKey to, float value, ArrayList<TransactionInput> inputs) {
		this.sender = from;
		this.reciepient = to;
		this.value = value;
		this.inputs = inputs;
	}

	// 새로운 트랜젝션 생성가능시 true 반환
	public boolean processTransaction() {

		if (verifiySignature() == false) {
			System.out.println("#Transaction Signature failed to verify");
			return false;
		}

		// 입력된 거래 수집
		for (TransactionInput i : inputs) {
			i.UTXO = YeobChain.UTXOs.get(i.transactionOutputId);
		}

		// 거래가 유효한지 확인
		if (getInputsValue() < YeobChain.minimumTransaction) {
			System.out.println("#Transaction Inputs to small: " + getInputsValue());
			return false;
		}

		// 거래 산출물 생성
		float leftOver = getInputsValue() - value; // 입력 값을 얻은 다음 leftOver 변경
		transactionId = calulateHash(); // 받는 이에게 value 전달
		outputs.add(new TransactionOutput(this.reciepient, value, transactionId)); // left over 변경하여 보낸이에게 back
		outputs.add(new TransactionOutput(this.sender, leftOver, transactionId));

		// 미사용 리스트에 추가
		for (TransactionOutput o : outputs) {
			YeobChain.UTXOs.put(o.id, o);
		}

		// UTXO 목록에서 트랜잭션된 값을 제거
		for (TransactionInput i : inputs) {
			if (i.UTXO == null)
				continue;
			YeobChain.UTXOs.remove(i.UTXO.id);
		}

		return true;

	}

	// 입력(UTXO)합 리턴
	public float getInputsValue() {
		float total = 0;
		for (TransactionInput i : inputs) {
			if (i.UTXO == null)
				continue;
			total += i.UTXO.value;
		}
		return total;
	}

	// 출력값 총합 리턴
	public float getOutputsValue() {
		float total = 0;
		for (TransactionOutput o : outputs) {
			total += o.value;
		}
		return total;
	}

	// transaction hash 계산
	private String calulateHash() {
		sequence++;
		return StringUtil.applySha256(StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(reciepient)
				+ Float.toString(value) + sequence);
	}

	// 변경하려는 모든 데이터에 서명
	public void generateSignature(PrivateKey privateKey) {
		String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(reciepient)
				+ Float.toString(value);
		signature = StringUtil.applyECDSASig(privateKey, data);
	}

	// 서명된 데이터 변경여부 확인
	public boolean verifiySignature() {
		String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(reciepient)
				+ Float.toString(value);
		return StringUtil.verifyECDSASig(sender, data, signature);
	}
}

