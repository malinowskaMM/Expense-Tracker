export interface AccountConfirmPasswordRequestDto {
    token: string;
    password: string;
    repeatPassword: string;
}