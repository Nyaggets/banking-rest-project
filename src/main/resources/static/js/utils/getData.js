const URL_BASE = 'http://localhost:8080';

const getData = async(url) => {
    try {
        const response = await fetch(url);
        if (response.ok) {
            return response.json()
        }
    }
    catch (error) {
        console.log(error)
    }
}

const client = await getData(`${URL_BASE}/clients/me`)
const cards = Array.from(await getData(`${URL_BASE}/clients/${client.id}/cards`))
const history = Array.from(await getData(`${URL_BASE}/clients/${client.id}/history`))

history.forEach(transaction => {
    switch (transaction.type) {
        case 'Оплата товаров и услуг':
            transaction.direction = 'out'
            transaction.counterpartyName = transaction.merchant
            break;

        case 'Зачисление':
            transaction.direction = 'in'
            transaction.counterpartyName = transaction.source
            break;

        case 'Перевод':
            if (transaction.receiverDetails.id == transaction.senderDetails.id) {
                transaction.direction = 'between'
                transaction.type = 'Перевод между своими'
                transaction.counterpartyName = client.name
                transaction.counterpartyNumber = transaction.senderCardNumber
            }
            else if (transaction.receiverDetails.id == client.id) {
                transaction.direction = 'in'
                transaction.type = 'Входящий перевод'
                transaction.counterpartyName = transaction.senderDetails.name
                transaction.counterpartyNumber = transaction.senderCardNumber
            }
            else if (transaction.senderDetails.id == client.id) {
                transaction.direction = 'out'
                transaction.counterpartyName = transaction.receiverDetails.name
                transaction.counterpartyNumber = transaction.receiverCardNumber
            }
            break;
    }
})

export { URL_BASE, getData, client, cards, history }