export function slugify(text: string) {
	return (
		text
			.normalize("NFD")
			// biome-ignore lint/suspicious/noMisleadingCharacterClass: <explanation>
			.replace(/[\u0300-\u036f]/g, "") // Remove acentos combináveis
			.toLowerCase()
			.trim()
			.replace(/\s+/g, "-") // espaços -> hífen
			.replace(/[^\w-]+/g, "") // remove tudo exceto letras/números/hífen
			.replace(/--+/g, "-")
	); // remove múltiplos hífens
}

