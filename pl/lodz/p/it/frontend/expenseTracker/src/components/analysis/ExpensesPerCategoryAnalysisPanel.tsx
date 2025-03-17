import React from 'react';
import './ExpensesAndIncomesAnalysisPanel.css';
import { DemoContainer } from '@mui/x-date-pickers/internals/demo';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { Select, MenuItem, Stack, Alert, AlertTitle, Button, Box, TableContainer, Table, TableHead, TableRow, TableCell, TableBody, TableFooter, TablePagination, Paper } from '@mui/material';
import { useState } from 'react';
import { GroupResponseDto } from '../../types/GroupResponseDto';
import { useTranslation } from 'react-i18next';
import axios from 'axios';
import { useEffect } from 'react';
import { API_URL } from '../../consts';
import emptyAnalysis from '../../assets/emptyAnalysis.png';
import dayjs, { Dayjs } from 'dayjs';
import { Chart as ChartJS, ArcElement, Tooltip, Legend, CategoryScale, LinearScale, PointElement, LineElement, Title } from 'chart.js';
import { Bar } from 'react-chartjs-2';
import { Pie } from 'react-chartjs-2';
import { Doughnut } from 'react-chartjs-2';
import { styled } from '@mui/system';

ChartJS.register(
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    Title,
    Tooltip,
    Legend,
    ArcElement,
    Tooltip,
    Legend
);

interface Transaction {
    id: string;
    name: string;
    categoryName: string;
    categoryColor: string;
    categoryId: string;
    groupName: string;
    isCyclic: boolean;
    period: number;
    periodUnit: string;
    date: string;
    endDate: string | null;
    amount: number;
    accountId: string;
    type: string;
    sign: string;
    version: string;
}

interface TransactionUnit {
    name: string;
    categoryName: string;
    categoryId: string;
    groupName: string;
    groupId: string;
    date: string;
    transactionType: string;
    isCyclic: boolean;
    period: number;
    periodUnit: string;
    endDate: string | null;
    amount: number;
}

interface ExpensePerCategory {
    categoryName: string;
    categoryColor: string;
    categoryDescription: string;
    percentage: string;
}

interface ExpensesData {
    expensesPerCategoryWithPercentage: ExpensePerCategory[];
    transactions: { transactions: Transaction[] };
    balance: number;
    startDate: string;
    endDate: string;
}

interface ExpensesPerType {
    balance: number;
    startDate: string;
    endDate: string;
    oneTimeTransactions: TransactionUnit[];
    cyclicTransactions: TransactionUnit[];
}


interface ChartData {
    labels: string[];
    datasets: {
        label: string;
        data: number[];
        backgroundColor: string[];
        borderColor: string[];
        borderWidth: number;
    }[];
}

const ExpensesPerCategoryAnalysisPanel = () => {
    const [groups, setGroups] = React.useState<GroupResponseDto[]>([]);
    const [group, setGroup] = useState('');
    const [validationErrors, setValidationErrors] = useState<string[]>([]);
    const [alertHeight, setAlertHeight] = useState(0);
    const { t, i18n } = useTranslation();
    const [startDate, setStartDate] = React.useState<Dayjs | null>(null);
    const [endDate, setEndDate] = React.useState<Dayjs | null>(null);
    const [chart, setChart] = React.useState<ChartData>({
        labels: [],
        datasets: [
            {
                label: '# of Expenses',
                data: [],
                backgroundColor: [],
                borderColor: [],
                borderWidth: 1,
            },
        ],
    });
    const [typeChart, setTypeChart] = React.useState<ChartData>({
        labels: [],
        datasets: [
            {
                label: '# of Expenses',
                data: [],
                backgroundColor: [],
                borderColor: [],
                borderWidth: 1,
            },
        ],
    });
    const [balance, setBalance] = React.useState<number>(0);
    const [startDateString, setStartDateString] = React.useState<string>('');
    const [endDateString, setEndDateString] = React.useState<string>('');
    const [transactions, setTransactions] = React.useState<Transaction[]>([]);
    const [page, setPage] = React.useState(0);
    const [rowsPerPage, setRowsPerPage] = React.useState(5);
    const [rows, setRows] = React.useState<Transaction[]>([]);
    const [expenseData, setExpenseData] = React.useState(null);



    const updateValidationErrors = (field: string, isValid: boolean) => {
        setValidationErrors((prevErrors) => {
            const updatedErrors = { ...prevErrors };
            if (!isValid) {
                switch (field) {
                    case 'group':
                        updatedErrors[field] = t('analysisPanel.groupValidation');
                        break;
                    case 'startDate':
                        updatedErrors[field] = t('analysisPanel.startDateValidation');
                        break;
                    case 'endDate':
                        updatedErrors[field] = t('analysisPanel.endDateValidation');
                        break;
                    case 'dateOrder':
                        updatedErrors[field] = t('analysisPanel.dateOrderValidation');
                        break;
                    case 'noResult':
                        updatedErrors[field] = t('analysisPanel.noResultValidation');
                        break;
                    default:
                        break;
                }
            } else {
                delete updatedErrors[field];
            }

            return updatedErrors;
        });
    }

    const validateGroup = (group: string) => {
        const isValid = group !== '';
        updateValidationErrors('group', isValid);
        return isValid;
    }

    const handleGroupChange = (event: React.ChangeEvent<{ value: unknown }>) => {
        setGroup(event.target.value as string);
        validateGroup(event.target.value as string);
    };

    const validateStartDate = (startDate: Dayjs | null) => {
        const isValid = startDate !== null;
        updateValidationErrors('startDate', isValid);
        return isValid;
    }

    const validateEndDate = (endDate: Dayjs | null) => {
        const isValid = endDate !== null;
        updateValidationErrors('endDate', isValid);
        return isValid;
    }

    const validateDateProperOrder = (startDate: Dayjs | null, endDate: Dayjs | null) => {
        const isValid = startDate !== null && endDate !== null && startDate.isBefore(endDate);
        updateValidationErrors('dateOrder', isValid);
        return isValid;
    }

    const onSubmit = () => {
        const isValidGroup = validateGroup(group);
        const isValidStartDate = validateStartDate(startDate);
        const isValidEndDate = validateEndDate(endDate);
        if (startDate !== null && endDate !== null) {
            const isValidDateOrder = validateDateProperOrder(startDate, endDate);
            if (isValidGroup && isValidStartDate && isValidEndDate && isValidDateOrder) {
                handleAnalysis();
                return;
            }
        }
        if (isValidGroup && isValidStartDate && isValidEndDate) {
            return;
        }
    }

    const handleAlertLoad = (event: React.ChangeEvent<HTMLInputElement>) => {
        setAlertHeight(event.target.clientHeight);
    };

    let configGroupData = {
        method: 'GET',
        url: API_URL + '/groups/account/' + localStorage.getItem("id"),
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + localStorage.getItem('token')
        },
    };

    const featchGroupData = async () => {
        axios.request(configGroupData)
            .then((response) => {
                setGroups(response.data.groups);
            }).catch((error) => {
            })
    }

    useEffect(() => {
        featchGroupData();
    }, []);

    const boxStyle = {
        display: 'flex',
        marginTop: '5%',
        justifyContent: 'center',
        width: '100%',
        textAlign: 'center',
        flexDirection: 'column',
        alignItems: 'center',
    };

    const buttonTextStyle = {
        textAlign: 'center' as const,
        backgroundColor: 'rgba(131, 225, 180, 1)',
        borderRadius: '5vh',
        fontSize: '1.0rem',
        color: 'rgb(43, 43, 43)',
        padding: '5%',
    };

    const headerTextStyle = {
        textAlign: 'center' as const,
        fontSize: '2.5rem',
        color: 'rgba(43, 42, 42, 0.7)',
    };

    const descriptionTextStyle = {
        textAlign: 'center' as const,
        fontSize: '1.0rem',
        color: 'rgba(43, 42, 42, 0.7)',
    };

    const StyledBox = styled(Box)({
        display: 'inline-block',
        border: '1px solid white',
        padding: '3%',
        borderRadius: '5vh',
        color: "#1E1E1E",
        fontSize: '0.7rem',
        textTransform: 'uppercase',
        textAlign: 'center' as const,
        fontFamily: [
            "Inter",
            'sans-serif',
        ].join(','),
    })


    const StyledGroupBox = styled(StyledBox)({
        backgroundColor: '#E2E5E3',
        color: '#1E1E1E',
        fontWeight: 'bold',
        textTransform: 'none',
    })

    const StyledCategoryBox = styled(StyledBox)({
        backgroundColor: '#E2E5E3',
        color: '#1E1E1E',
        fontWeight: 'lighter',
        textTransform: 'none',
    })

    const StyledIncomeTypeBox = styled(StyledBox)({
        backgroundColor: '#3CD270',
        color: '#1E1E1E',
        fontWeight: 'bold',
        textTransform: 'none',
    })

    const StyledExpenseTypeBox = styled(StyledBox)({
        backgroundColor: '#C95050',
        color: '#1E1E1E',
        fontWeight: 'bold',
        textTransform: 'none',
    })

    function transformHttpDataToChartData(httpData: ExpensesData): ChartData {
        const categories = httpData.expensesPerCategoryWithPercentage.map(
            (category) => category.categoryName
        );
        const amounts = categories.map((category) =>
            httpData.transactions.transactions.reduce((total, transaction) => {
                if (transaction.categoryName === category && transaction.type === "EXPENSE") {
                    total += transaction.amount;
                }
                return total;
            }, 0)
        );

        const backgroundColors = httpData.expensesPerCategoryWithPercentage.map(
            (category) => category.categoryColor
        );
        const borderColors = backgroundColors;

        const descriptions = httpData.expensesPerCategoryWithPercentage.map(
            (category) => category.categoryDescription
        );

        return {
            labels: categories,
            datasets: [
                {
                    label: "# of Expenses",
                    data: amounts,
                    backgroundColor: backgroundColors,
                    borderColor: borderColors,
                    borderWidth: 1,
                },
            ],
        };
    }


    function transformHttpDataToTypeChartData(httpData: ExpensesPerType): ChartData {
        const oneTimeTransactions = httpData.oneTimeTransactions.length;

        const cyclicTransactions = httpData.cyclicTransactions.length;

        const amounts = [oneTimeTransactions, cyclicTransactions];
        const backgroundColors = ['#3CD270', '#C95050'];
        const borderColors = backgroundColors;


        return {
            labels: [t('analysisPanel.oneTimeExpenses'), t('analysisPanel.recurringExpenses')],
            datasets: [
                {
                    label: "# of Expenses",
                    data: amounts,
                    backgroundColor: backgroundColors,
                    borderColor: borderColors,
                    borderWidth: 1,
                },
            ],
        };
    }

    const emptyRows =
        page > 0 ? Math.max(0, (1 + page) * rowsPerPage - rows.length) : 0;

    const handleChangePage = (
        event: React.MouseEvent<HTMLButtonElement> | null,
        newPage: number,
    ) => {
        setPage(newPage);
    };

    const handleChangeRowsPerPage = (
        event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>,
    ) => {
        setRowsPerPage(parseInt(event.target.value, 10));
        setPage(0);
    };

    const handleAnalysis = () => {
        handleAnalysisDataProccess();
        handleAnalysisExpenseDataProccess();
        handleAnalysisDataProccessPerType();
    }

    const handleAnalysisDataProccess = () => {
        let config = {
            method: 'POST',
            url: API_URL + '/analysis/expenses/perCategory',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            },
            data: {
                groupId: group,
                startDate: startDate.format('YYYY-MM-DD'),
                endDate: endDate.format('YYYY-MM-DD')
            }
        };


        axios.request(config)
            .then((response) => {
                setChart(transformHttpDataToChartData(response.data));
                setBalance(response.data.balance);
                setStartDateString(response.data.startDate);
                setEndDateString(response.data.endDate);
                setRows(response.data.transactions.transactions);
            }).catch((error) => {
                updateValidationErrors('noResult', false);
            })
    }

    const handleAnalysisDataProccessPerType = () => {
        let config = {
            method: 'POST',
            url: API_URL + '/analysis/expenses/perType',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            },
            data: {
                groupId: group,
                startDate: startDate.format('YYYY-MM-DD'),
                endDate: endDate.format('YYYY-MM-DD')
            }
        };


        axios.request(config)
            .then((response) => {
                setTypeChart(transformHttpDataToTypeChartData(response.data));
            }).catch((error) => {
                updateValidationErrors('noResult', false);
            })
    }


    const handleAnalysisExpenseDataProccess = () => {
        let config = {
            method: 'POST',
            url: API_URL + '/analysis/expenses',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            },
            data: {
                groupId: group,
                startDate: startDate.format('YYYY-MM-DD'),
                endDate: endDate.format('YYYY-MM-DD')
            }
        };

        axios.request(config)
            .then((response) => {
                const labels = Object.keys(response.data.expensesPerDay);

                labels.sort((a, b) => {
                    // Przekonwertuj stringi na obiekty Date
                    const dateA = new Date(a);
                    const dateB = new Date(b);

                    // Porównaj daty
                    if (dateA < dateB) return -1;
                    if (dateA > dateB) return 1;
                    return 0;
                });

                const data = {
                    labels,
                    datasets: [
                        {
                            label: t('analysisPanel.expenses'),
                            data: labels.map(date => response.data.expensesPerDay[date].reduce((total, transaction) => total + transaction.amount, 0)),
                            borderColor: 'rgb(255, 99, 132)',
                            backgroundColor: 'rgba(255, 99, 132, 0.5)',
                        },
                    ],
                };
                setExpenseData(data);
            }).catch((error) => {
                updateValidationErrors('noResult', false);
            })
    }

    const optionsLineChart = {
        responsive: true,
        plugins: {
            legend: {
                position: 'top' as const,
            },
            title: {
                display: true,
                text: t('analysisPanel.lineChartTitle'),
            },
        },
    };

    const optionsPieChart = {
        responsive: true,
        plugins: {
            legend: {
                position: 'top' as const,
            },
            title: {
                display: true,
                text: t('analysisPanel.pieChartTitle'),
            },
        },
    };

    const optionsTypePieChart = {
        responsive: true,
        plugins: {
            legend: {
                position: 'top' as const,
            },
            title: {
                display: true,
                text: t('analysisPanel.typePieChartTitle'),
            },
        },
    };

    return (
        <div className="container">
            <div className="green-column">
                <div style={{ marginTop: '10%', marginLeft: '5%', marginRight: '5%', alignContent: 'center', justifyItems: 'center' }}>

                    <div className="font-link" style={{ marginBottom: '5%' }}>
                        {t('analysisPanel.choseGroup')}*
                    </div>
                    <Select
                        labelId="demo-simple-select-label"
                        id="group-select"
                        value={group}
                        label="Group"
                        onChange={handleGroupChange}
                        style={{ width: '100%' }}
                    >
                        {groups.map((groupItem) => (
                            <MenuItem key={groupItem.groupId} value={groupItem.groupId}>
                                {groupItem.groupName}
                            </MenuItem>
                        ))}
                    </Select>


                    <div className="font-link" style={{ marginBottom: '5%', marginTop: '5%' }}>
                        {t('analysisPanel.choseStartDate')}*
                    </div>
                    <div className="date-picker-container">
                        <LocalizationProvider dateAdapter={AdapterDayjs}>
                            <DemoContainer components={['DatePicker']}>
                                <DatePicker label={t('analysisPanel.startDate')}
                                    value={startDate}
                                    onChange={(newStartDate) => {
                                        setStartDate(newStartDate)
                                        validateStartDate(newStartDate)
                                    }
                                    }
                                />
                            </DemoContainer>
                        </LocalizationProvider>
                    </div>


                    <div className="font-link" style={{ marginBottom: '5%', marginTop: '5%' }}>
                        {t('analysisPanel.choseEndDate')}*
                    </div>
                    <div className="date-picker-container">
                        <LocalizationProvider dateAdapter={AdapterDayjs}>
                            <DemoContainer components={['DatePicker']}>
                                <DatePicker label={t('analysisPanel.endDate')}
                                    value={endDate}
                                    onChange={(newEndDate) => {
                                        setEndDate(newEndDate)
                                        validateEndDate(newEndDate)
                                    }
                                    }
                                />
                            </DemoContainer>
                        </LocalizationProvider>
                    </div>

                    <div className="font-link" style={{ marginTop: '10%', textAlign: 'center' }}>
                        <Button onClick={onSubmit}>
                            <div className="font-link" style={buttonTextStyle}>
                                {t('analysisPanel.generateReport')}
                            </div>
                        </Button>
                    </div>

                    <div className="font-link" style={{ ...headerTextStyle, marginBottom: '5%', marginTop: '5%', display: 'block', paddingTop: '10%' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                            <div style={{ fontWeight: 'bold' }}>
                                {t('analysisPanel.balance')}
                            </div>
                            <div style={{ textAlign: 'right', paddingTop: '10%' }}>
                                {balance} PLN
                            </div>
                        </div>
                    </div>
                    <div style={descriptionTextStyle}>
                        {t('analysisPanel.balanceDescription')}
                    </div>
                </div>
            </div>
            <div className="content-column"> {/* Druga kolumna 75% szerokości */}
                {/* Twój zawartość dla drugiej kolumny */}
                <Box sx={boxStyle}>

                    {chart.labels.length <= 0 ?
                        <>
                            <img src={emptyAnalysis} alt='empty' style={{ width: '35%' }} />
                            <div className="font-link" style={{ ...headerTextStyle, marginBottom: '5%', marginTop: '5%', display: 'block' }}>
                                {t('analysisPanel.noResultForData')}
                            </div>
                        </> :

                        <>
                            <div className="rectangle">
                                <div className="square-container">
                                    <div className="rectangle1"> <Doughnut data={typeChart} options={optionsTypePieChart} /> </div>
                                    <div className="rectangle1"> <Pie data={chart} options={optionsPieChart} /></div>
                                </div>
                                <div className="square-container">
                                    <div className="rectangle3"> <Bar data={expenseData} options={optionsLineChart} /> </div>
                                </div></div>
                            <div className="rectangle" style={{ paddingTop: '20px' }}>
                                <TableContainer component={Paper}>
                                    <Table aria-label="custom pagination table">
                                        <TableHead>
                                            <TableRow >
                                                <TableCell>
                                                    <div className="font-link-table-header">
                                                        {t('calendarPanel.name')}
                                                    </div>
                                                </TableCell>
                                                <TableCell>
                                                    <div className="font-link-table-header" style={{ textAlign: 'left' }}>
                                                        {t('calendarPanel.group')}
                                                    </div>
                                                    <div className="font-link-table-header" style={{ textAlign: 'left' }}>
                                                        {t('calendarPanel.category')}
                                                    </div>
                                                </TableCell>
                                                <TableCell>
                                                    <div className="font-link-table-header">
                                                        {t('calendarPanel.type')}
                                                    </div>
                                                </TableCell>
                                                <TableCell>
                                                    <div className="font-link-table-header">
                                                        {t('analysisPanel.isCyclic')}
                                                    </div>
                                                </TableCell>
                                                <TableCell>
                                                    <div className="font-link-table-header">
                                                        {t('analysisPanel.period')}
                                                    </div>
                                                </TableCell>
                                                <TableCell>
                                                    <div className="font-link-table-header">
                                                        {t('analysisPanel.periodUnit')}
                                                    </div>
                                                </TableCell>
                                                <TableCell>
                                                    <div className="font-link-table-header">
                                                        {t('calendarPanel.amount')}
                                                    </div>
                                                </TableCell>
                                            </TableRow>
                                        </TableHead>
                                        <TableBody>
                                            {(rowsPerPage > 0
                                                ? rows.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                                                : rows
                                            ).map((row) => (
                                                <TableRow key={row.id}>
                                                    <TableCell component="th" scope="row">
                                                        {row.name}
                                                    </TableCell>
                                                    <TableCell align="left">
                                                        <div style={{ display: 'table-row' }}>
                                                            <StyledGroupBox>
                                                                {row.groupName}
                                                            </StyledGroupBox>
                                                            <StyledCategoryBox>
                                                                {row.categoryName}
                                                            </StyledCategoryBox>
                                                        </div>
                                                    </TableCell>
                                                    <TableCell align="left">
                                                        {row.type === 'INCOME' ? (
                                                            <StyledIncomeTypeBox>{t('calendarPanel.income')}</StyledIncomeTypeBox>
                                                        ) : (
                                                            <StyledExpenseTypeBox>{t('calendarPanel.expense')}</StyledExpenseTypeBox>
                                                        )}
                                                    </TableCell>
                                                    <TableCell align="left">
                                                        {row.isCyclic ? t('analysisPanel.yes') : t('analysisPanel.no')}
                                                    </TableCell>
                                                    <TableCell align="left">
                                                        {row.period}
                                                    </TableCell>
                                                    <TableCell align="left">
                                                        {row.periodUnit == 'DAY' ? t('analysisPanel.day') : row.periodUnit == 'MONTH' ? t('analysisPanel.month') : row.periodUnit == 'YEAR' ? t('analysisPanel.year') : ''}
                                                    </TableCell>
                                                    <TableCell align="left">
                                                        {row.amount}
                                                    </TableCell>
                                                </TableRow>
                                            ))}
                                            {emptyRows > 0 && (
                                                <TableRow style={{ height: 53 * emptyRows }}>
                                                    <TableCell colSpan={5} />
                                                </TableRow>
                                            )}
                                        </TableBody>
                                        <TableFooter>
                                            <TableRow>
                                                <TablePagination
                                                    rowsPerPageOptions={[1, 5, 10, 25, { label: t('common.all'), value: -1 }]}
                                                    colSpan={3}
                                                    count={rows.length}
                                                    rowsPerPage={rowsPerPage}
                                                    page={page}
                                                    labelRowsPerPage={t('common.rowsPerPage')}
                                                    SelectProps={{

                                                        native: true,
                                                    }}
                                                    onPageChange={handleChangePage}
                                                    onRowsPerPageChange={handleChangeRowsPerPage}
                                                //ActionsComponent={TablePagination}
                                                />
                                            </TableRow>
                                        </TableFooter>
                                    </Table>
                                </TableContainer>
                            </div>
                        </>
                    }

                </Box>
            </div>


            <Stack sx={{
                width: '20%',
                position: 'fixed',
                bottom: 16 + alertHeight,
                right: 16,
                zIndex: 1000,
            }} spacing={2}>
                {Object.values(validationErrors).map((error, index) => (
                    <Alert key={index} severity="error" onLoad={handleAlertLoad}>
                        <AlertTitle>{t('categoryPanel.formValidation')}</AlertTitle>
                        {error}
                    </Alert>
                ))}
            </Stack>
        </div>
    );
}

export default ExpensesPerCategoryAnalysisPanel;