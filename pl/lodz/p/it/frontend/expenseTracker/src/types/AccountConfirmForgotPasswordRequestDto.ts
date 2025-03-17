export interface AccountConfirmForgotPasswordRequestDto {
    password: string;
    repeatPassword: string;
    token: string;
}