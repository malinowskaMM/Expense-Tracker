export interface AccountResponseDto {
    id: number;
    email: string;
    active: boolean;
    enable: boolean;
    registerDate: Date;
    lastLoginDate: Date;
    lastInvalidLoginDate: Date;
    role: string;
}