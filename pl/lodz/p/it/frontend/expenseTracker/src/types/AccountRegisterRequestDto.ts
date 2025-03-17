export interface AccountRegisterRequestDto {
    email: string;
    username: string;
    password: string;
    repeatPassword: string;
    language_: string;
    callbackRoute: string;
}