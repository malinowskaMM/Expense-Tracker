export interface TransactionResponseDto {
    id: string;
    name: string;
    categoryName: string;
    categoryColor: string;
    groupName: string;
    isCyclic: boolean;
    period: number;
    periodUnit: string;
    date: string;
    endDate: string;
    amount: number;
    accountId: string;
    type: string;
    status: boolean;
}