export interface TransactionCreateRequestDto {
    name: string;
    cycle: string;
    period: string;
    periodType: string;
    type: string;
    categoryId: string;
    date: string;
    value: number;
    creatorId: string;
};