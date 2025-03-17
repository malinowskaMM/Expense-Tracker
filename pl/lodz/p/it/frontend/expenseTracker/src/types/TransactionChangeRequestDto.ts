export interface TransactionChangeRequestDto {
    name: string;
    cycle: string;
    period: number;
    periodType: string;
    type: string;
    categoryId: string;
    date: string;
    value: number;
    version: string;
}