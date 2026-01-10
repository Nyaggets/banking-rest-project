import { history } from './utils/getData.js'
import { showHistory } from './utils/processData.js'

const historyList = document.getElementById('history-list')
showHistory(history, historyList)