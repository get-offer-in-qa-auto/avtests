//Сделать успешный депозит юзером
BeforeEach
Залогиниться админом
Создать нового юзера
Авторизоваться юзером
Создать новый аккаунт
userCanMakeDepositTest()
1) Переходим на страницу компонента
*Отображается панель class="container mt-4 text-center"
2) Нажимаем на кнопку DepositMoney
* Вызывается роут api/v1/customer/accounts
* Получаем 200 код
* Отображается элемент class="container mt-4 text-center"
3) Нажимаем на панель Choose an account(class="form-control account-selector")
4) Выбираем аккаунт из списка
5)В Enter Amount(class="form-control deposit-input") вводим 4999.99
6)Нажимаем на кнопку Deposit
* Вызываем роут /api/v1/accounts/deposit
* Получаем 200 код
* Перенаправление на страницу Dashboard(class="container mt-4 text-center")


###
// Делаем депозит более допустимого
BeforeEach
Залогиниться админом
Создать нового юзера
Авторизоваться юзером
Создать новый аккаунт
userCanNotMakeDepositWithInvalidAmountTest()
1) Переходим на страницу компонента
*Отображается панель class="container mt-4 text-center"
2) Нажимаем на кнопку DepositMoney
* Вызывается роут api/v1/customer/accounts
* Получаем 200 код
* Отображается элемент class="container mt-4 text-center"
3) Нажимаем на панель Choose an account(class="form-control account-selector")
4) Выбираем аккаунт из списка
5)В Enter Amount(class="form-control deposit-input") вводим 5000.01
6)Нажимаем на кнопку Deposit
* Появление модального окна с ошибкой ❌ Please enter a valid amount.

###
// Делаем допустимый депозит. Граничное значение
BeforeEach
Залогиниться админом
Создать нового юзера
Авторизоваться юзером
Создать новый аккаунт
userCanMakeDepositBoarderTopAmountTest()
1) Переходим на страницу компонента
*Отображается панель class="container mt-4 text-center"
2) Нажимаем на кнопку DepositMoney
* Вызывается роут api/v1/customer/accounts
* Получаем 200 код
* Отображается элемент class="container mt-4 text-center"
3) Нажимаем на панель Choose an account(class="form-control account-selector")
4) Выбираем аккаунт из списка
5)В Enter Amount(class="form-control deposit-input") вводим 5000
6)Нажимаем на кнопку Deposit
* Вызываем роут /api/v1/accounts/deposit
* Получаем 200 код
* Перенаправление на страницу Dashboard(class="container mt-4 text-center")

###
// Делаем депозит Отрицательной суммой
BeforeEach
Залогиниться админом
Создать нового юзера
Авторизоваться юзером
Создать новый аккаунт
userCanNotMakeDepositWithMinusAmountTest()
1) Переходим на страницу компонента
*Отображается панель class="container mt-4 text-center"
2) Нажимаем на кнопку DepositMoney
* Вызывается роут api/v1/customer/accounts
* Получаем 200 код
* Отображается элемент class="container mt-4 text-center"
3) Нажимаем на панель Choose an account(class="form-control account-selector")
4) Выбираем аккаунт из списка
5)В Enter Amount(class="form-control deposit-input") вводим -0.01
6)Нажимаем на кнопку Deposit
* Появление модального окна с ошибкой ❌ Please enter a valid amount.

###
// Делаем допустимый депозит. Граничное значение
BeforeEach
Залогиниться админом
Создать нового юзера
Авторизоваться юзером
Создать новый аккаунт
userCanMakeDepositWithBoarderLowAmountTest()
1) Переходим на страницу компонента
*Отображается панель class="container mt-4 text-center"
2) Нажимаем на кнопку DepositMoney
* Вызывается роут api/v1/customer/accounts
* Получаем 200 код
* Отображается элемент class="container mt-4 text-center"
3) Нажимаем на панель Choose an account(class="form-control account-selector")
4) Выбираем аккаунт из списка
5)В Enter Amount(class="form-control deposit-input") вводим 0.01
6)Нажимаем на кнопку Deposit
* Вызываем роут /api/v1/accounts/deposit
* Получаем 200 код
* Перенаправление на страницу Dashboard(class="container mt-4 text-center")

###
// Делаем депозит с нулевой суммой
BeforeEach
Залогиниться админом
Создать нового юзера
Авторизоваться юзером
Создать новый аккаунт
userCanNotMakeDepositWithZeroAmountTest()
1) Переходим на страницу компонента
*Отображается панель class="container mt-4 text-center"
2) Нажимаем на кнопку DepositMoney
* Вызывается роут api/v1/customer/accounts
* Получаем 200 код
* Отображается элемент class="container mt-4 text-center"
3) Нажимаем на панель Choose an account(class="form-control account-selector")
4) Выбираем аккаунт из списка
5)В Enter Amount(class="form-control deposit-input") вводим 0
6)Нажимаем на кнопку Deposit
* Появление модального окна с ошибкой ❌ Please enter a valid amount.

###
// Перевод денег с одного аккаунта на другой
BeforeEach
Залогиниться админом
Создать нового юзера
Авторизоваться юзером
Создать новый аккаунт
Cоздать еще один аккаунт
userCanMakeTransferTest()
1) Переходим на страницу компонента
*Отображается панель class="container mt-4 text-center"
2) Нажимаем на кнопку Make a Transfer(button.toHaveText('Make a Transfer')
* Вызывается роут api/v1/customer/accounts
* Получаем 200 код
* Отображается элемент class="form-group mt-4"
3) Нажимаем на панель Choose an account(class="form-control account-selector")
4) Выбираем первый аккаунт из списка
5) В Enter recipient account number(placeholder="Enter recipient account number") вводим ACC2
6) В Enter Amount(placeholder="Enter amount") вводим 10000
7) Нажимаем на чекбокс class="form-check-input"
8)Нажимаем на кнопку Send Transfer(class="btn-primary shadow-custom green-btn mt-4")
* Вызываем роут /api/v1/accounts/transfer
* Получаем 200 код

###
// Перевод денег с одного аккаунта на другой. Граничные значения
BeforeEach
Залогиниться админом
Создать нового юзера
Авторизоваться юзером
Создать новый аккаунт
Cоздать еще один аккаунт
userCanMakeTransferWithBoarderAmountTest()
1) Переходим на страницу компонента
*Отображается панель class="container mt-4 text-center"
2) Нажимаем на кнопку Make a Transfer(button.toHaveText('Make a Transfer')
* Вызывается роут api/v1/customer/accounts
* Получаем 200 код
* Отображается элемент class="form-group mt-4"
3) Нажимаем на панель Choose an account(class="form-control account-selector")
4) Выбираем первый аккаунт из списка
5) В Enter recipient account number(placeholder="Enter recipient account number") вводим ACC2
6) В Enter Amount(placeholder="Enter amount") вводим 9999.99
7) Нажимаем на чекбокс class="form-check-input"
8)Нажимаем на кнопку Send Transfer(class="btn-primary shadow-custom green-btn mt-4")
* Вызываем роут /api/v1/accounts/transfer
* Получаем 200 код

###
// Перевод денег с одного аккаунта на другой. Граничные значения
BeforeEach
Залогиниться админом
Создать нового юзера
Авторизоваться юзером
Создать новый аккаунт
Cоздать еще один аккаунт
userCanMakeTransferWithBoarderAmountLowTest()
1) Переходим на страницу компонента
*Отображается панель class="container mt-4 text-center"
2) Нажимаем на кнопку Make a Transfer(button.toHaveText('Make a Transfer')
* Вызывается роут api/v1/customer/accounts
* Получаем 200 код
* Отображается элемент class="form-group mt-4"
3) Нажимаем на панель Choose an account(class="form-control account-selector")
4) Выбираем первый аккаунт из списка
5) В Enter recipient account number(placeholder="Enter recipient account number") вводим ACC2
6) В Enter Amount(placeholder="Enter amount") вводим 0.01
7) Нажимаем на чекбокс class="form-check-input"
8)Нажимаем на кнопку Send Transfer(class="btn-primary shadow-custom green-btn mt-4")
* Вызываем роут /api/v1/accounts/transfer
* Получаем 200 код

###
// Перевод денег с одного аккаунта на другой выше максимальной суммы
BeforeEach
Залогиниться админом
Создать нового юзера
Авторизоваться юзером
Создать новый аккаунт
Cоздать еще один аккаунт
userCanNotMakeTransferWithBiggerAmountTest()
1) Переходим на страницу компонента
*Отображается панель class="container mt-4 text-center"
2) Нажимаем на кнопку Make a Transfer(button.toHaveText('Make a Transfer')
* Вызывается роут api/v1/customer/accounts
* Получаем 200 код
* Отображается элемент class="form-group mt-4"
3) Нажимаем на панель Choose an account(class="form-control account-selector")
4) Выбираем первый аккаунт из списка
5) В Enter recipient account number(placeholder="Enter recipient account number") вводим ACC2
6) В Enter Amount(placeholder="Enter amount") вводим 10000.01
7) Нажимаем на чекбокс class="form-check-input"
8)Нажимаем на кнопку Send Transfer(class="btn-primary shadow-custom green-btn mt-4")
* Вызываем роут /api/v1/accounts/transfer
* Получаем 400 код
*Появление модального окна с текстом ❌ Error: Invalid transfer: insufficient funds or invalid accounts

###
// Перевод денег с одного аккаунта на другой с нулевым переводом
BeforeEach
Залогиниться админом
Создать нового юзера
Авторизоваться юзером
Создать новый аккаунт
Cоздать еще один аккаунт
userCanNotMakeTransferWithZeroAmountTest()
1) Переходим на страницу компонента
*Отображается панель class="container mt-4 text-center"
2) Нажимаем на кнопку Make a Transfer(button.toHaveText('Make a Transfer')
* Вызывается роут api/v1/customer/accounts
* Получаем 200 код
* Отображается элемент class="form-group mt-4"
3) Нажимаем на панель Choose an account(class="form-control account-selector")
4) Выбираем первый аккаунт из списка
5) В Enter recipient account number(placeholder="Enter recipient account number") вводим ACC2
6) В Enter Amount(placeholder="Enter amount") вводим 0
7) Нажимаем на чекбокс class="form-check-input"
8)Нажимаем на кнопку Send Transfer(class="btn-primary shadow-custom green-btn mt-4")
* Вызываем роут /api/v1/accounts/transfer
* Получаем 400 код
*Появление модального окна с текстом ❌ Error: Transfer amount must be at least 0.01

###
// Перевод денег с одного аккаунта на другой(отрицательная сумма перевода)
BeforeEach
Залогиниться админом
Создать нового юзера
Авторизоваться юзером
Создать новый аккаунт
Cоздать еще один аккаунт
userCanNotMakeTransferWithZeroAmountTest()
1) Переходим на страницу компонента
*Отображается панель class="container mt-4 text-center"
2) Нажимаем на кнопку Make a Transfer(button.toHaveText('Make a Transfer')
* Вызывается роут api/v1/customer/accounts
* Получаем 200 код
* Отображается элемент class="form-group mt-4"
3) Нажимаем на панель Choose an account(class="form-control account-selector")
4) Выбираем первый аккаунт из списка
5) В Enter recipient account number(placeholder="Enter recipient account number") вводим ACC2
6) В Enter Amount(placeholder="Enter amount") вводим -0.01
7) Нажимаем на чекбокс class="form-check-input"
8)Нажимаем на кнопку Send Transfer(class="btn-primary shadow-custom green-btn mt-4")
* Вызываем роут /api/v1/accounts/transfer
* Получаем 400 код
*Появление модального окна с текстом ❌ Error: Transfer amount must be at least 0.01

###
// Валидное Изменение имени пользователя
BeforeEach
Залогиниться админом
Создать нового юзера
Авторизоваться юзером
userCanChangeNameTest()
1) Переходим на страницу компонента
*Отображается панель class="container mt-4 text-center"
2) Нажимаем на кнопку с именем пользователя(class="user-info")
* Вызывается роут api/v1/customer/profile
* Получаем 200 код
* Кнопка Save Changes доступна(class="btn btn-primary mt-3"
3) В Enter New Name(placeholder="Enter new name") вводим Alex Petrov
4) Нажимаем на Save Changes(class="btn btn-primary mt-3")
* Вызывается роут /api/v1/customer/profile
* Получаем 200 код
5) Перезагружаем страницу
* Проверяем, что в элементе class="form-control mt-3"hasValue="Alex Petrov"

###
// Изменение имени пользователя(пустая строка)
BeforeEach
Залогиниться админом
Создать нового юзера
Авторизоваться юзером
userCanNotChangeToBlankNameTest()
1) Переходим на страницу компонента
*Отображается панель class="container mt-4 text-center"
2) Нажимаем на кнопку с именем пользователя(class="user-info")
* Вызывается роут api/v1/customer/profile
* Получаем 200 код
* Кнопка Save Changes доступна(class="btn btn-primary mt-3"
3) Enter New Name(placeholder="Enter new name") оставляем пустым
4) Нажимаем на Save Changes(class="btn btn-primary mt-3")
* Появление модального окна с текстом ❌ Please enter a valid name.

###
// Изменение имени пользователя(Имеются цифры)
BeforeEach
Залогиниться админом
Создать нового юзера
Авторизоваться юзером
userCanNotChangeNameWithNumbersTest()
1) Переходим на страницу компонента
*Отображается панель class="container mt-4 text-center"
2) Нажимаем на кнопку с именем пользователя(class="user-info")
* Вызывается роут api/v1/customer/profile
* Получаем 200 код
* Кнопка Save Changes доступна(class="btn btn-primary mt-3"
3) В Enter New Name(placeholder="Enter new name") вводим Alex6788
4) Нажимаем на Save Changes(class="btn btn-primary mt-3")
* Вызывается роут /api/v1/customer/profile
* Получаем 400 код
* Появление модального окна с текстом Name must contain two words with letters only

###
// Изменение имени пользователя(Имеются спецсимволы)
BeforeEach
Залогиниться админом
Создать нового юзера
Авторизоваться юзером
userCanNotChangeNameWithSymbolsTest()
1) Переходим на страницу компонента
*Отображается панель class="container mt-4 text-center"
2) Нажимаем на кнопку с именем пользователя(class="user-info")
* Вызывается роут api/v1/customer/profile
* Получаем 200 код
* Кнопка Save Changes доступна(class="btn btn-primary mt-3"
3) В Enter New Name(placeholder="Enter new name") вводим Alex Petrov$
4) Нажимаем на Save Changes(class="btn btn-primary mt-3")
* Вызывается роут /api/v1/customer/profile
* Получаем 400 код
* Появление модального окна с текстом Name must contain two words with letters only

###
// Изменение имени пользователя(Нет Пробела). Найден Баг
BeforeEach
Залогиниться админом
Создать нового юзера
Авторизоваться юзером
userCanNotChangeNameWithoutSpaceTest()
1) Переходим на страницу компонента
*Отображается панель class="container mt-4 text-center"
2) Нажимаем на кнопку с именем пользователя(class="user-info")
* Вызывается роут api/v1/customer/profile
* Получаем 200 код
* Кнопка Save Changes доступна(class="btn btn-primary mt-3"
3) В Enter New Name(placeholder="Enter new name") вводим AlexPetrov
4) Нажимаем на Save Changes(class="btn btn-primary mt-3")
* Вызывается роут /api/v1/customer/profile
* Получаем 400 код
* Появление модального окна с текстом Name must contain two words with letters only