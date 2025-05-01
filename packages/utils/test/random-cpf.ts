export function generateCPF() {
	const randomDigits = () =>
		Array.from({ length: 9 }, () => Math.floor(Math.random() * 10));

	const calcCheckDigit = (digits: number[]) => {
		const sum = digits.reduce(
			(acc, digit, i) => acc + digit * (digits.length + 1 - i),
			0,
		);
		const remainder = sum % 11;
		return remainder < 2 ? 0 : 11 - remainder;
	};

	const cpf = randomDigits();
	cpf.push(calcCheckDigit(cpf));
	cpf.push(calcCheckDigit(cpf));

	return cpf.join("");
}
