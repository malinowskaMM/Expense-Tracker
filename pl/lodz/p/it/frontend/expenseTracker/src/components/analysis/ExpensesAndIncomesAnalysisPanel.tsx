import React from 'react';
import './ExpensesAndIncomesAnalysisPanel.css';
import { DemoContainer } from '@mui/x-date-pickers/internals/demo';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { Select, MenuItem, Stack, Alert, AlertTitle, Button, TableContainer, Paper, Table, TableBody, TableFooter, TablePagination, TableRow, TableHead, TableCell } from '@mui/material';
import { useState } from 'react';
import { GroupResponseDto } from '../../types/GroupResponseDto';
import { useTranslation } from 'react-i18next';
import axios from 'axios';
import { useEffect } from 'react';
import { API_URL } from '../../consts';
import Box from '@mui/material/Box';
import emptyAnalysis from '../../assets/emptyAnalysis.png';
import dayjs, { Dayjs } from 'dayjs';
import { Bar } from 'react-chartjs-2';
import { Line } from 'react-chartjs-2';
import { styled } from '@mui/material/styles';
import {
    Chart as ChartJS,
    CategoryScale,
    LinearScale,
    BarElement,
    Title,
    Tooltip,
    Legend
} from 'chart.js';

ChartJS.register(
    CategoryScale,
    LinearScale,
    BarElement,
    Title,
    Tooltip,
    Legend
);

const ExpensesAndIncomesAnalysisPanel = () => {
    const [groups, setGroups] = React.useState<GroupResponseDto[]>([]);
    const [group, setGroup] = useState('');
    const [validationErrors, setValidationErrors] = useState<string[]>([]);
    const [alertHeight, setAlertHeight] = useState(0);
    const { t, i18n } = useTranslation();
    const [startDate, setStartDate] = React.useState<Dayjs | null>(null);
    const [endDate, setEndDate] = React.useState<Dayjs | null>(null);
    const [balance, setBalance] = React.useState<number>(0);
    const [expensesValue, setExpensesValue] = React.useState<number>(0);
    const [incomesValue, setIncomesValue] = React.useState<number>(0);
    const [startDateString, setStartDateString] = React.useState<string>('');
    const [endDateString, setEndDateString] = React.useState<string>('');
    const [dataSet, setDataSet] = React.useState<any>([]);
    const [generated, setGenerated] = React.useState<boolean>(false);
    const [transactionData, setTransactionData] = React.useState<any>([]);
    const [dateWithBiggestExpense, setDateWithBiggestExpense] = React.useState<string>('');
    const [rows, setRows] = React.useState<any[]>([]);
    const [page, setPage] = React.useState(0);
    const [rowsPerPage, setRowsPerPage] = React.useState(5);

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

                handleAnalysisDataProccess();
                handleAnalysisDataProccessPerDay();
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
        color: 'rgba(245, 249, 246, 1)',
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

    const handleAnalysisDataProccess = () => {
        let config = {
            method: 'POST',
            url: API_URL + '/analysis/balance/expenses/incomes',
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
                const labels = [response.data.startDate + " - " + response.data.endDate];
                const data =
                {
                    labels: labels,
                    datasets: [
                        {
                            label: t('analysisPanel.expenses'),
                            data: labels.map(() => (-1) * response.data.expensesValue),
                            borderColor: '#e15c5c',
                            backgroundColor: '#eb6666',
                        },
                        {
                            label: t('analysisPanel.incomes'),
                            data: labels.map(() => response.data.incomesValue),
                            borderColor: '#7ed0a8',
                            backgroundColor: '#8dd7b3',
                        },
                    ],
                };
                setExpensesValue(response.data.expensesValue);
                setIncomesValue(response.data.incomesValue);
                setBalance(response.data.balance);
                setStartDateString(response.data.startDate);
                setEndDateString(response.data.endDate);
                setGenerated(true);
                setDataSet(data);
            }).catch((error) => {
                console.log(error);
                setGenerated(false);
            })
    }

    const options = {
        plugins: {
            title: {
                display: true,
                text: t('analysisPanel.incomesAndExpensesChartTitle') + ' ' + startDateString + " - " + endDateString,
            },
        },
        responsive: true,
        scales: {
            x: {
                title: {
                    display: true,
                    text: t('analysisPanel.incomesAndExpensesChartXAxis'),
                },
                stacked: true,
            },
            y: {
                title: {
                    display: true,
                    text: t('analysisPanel.incomesAndExpensesChartYAxis'),
                },
                stacked: true,
            },
        },
    };

    const transactionsOptions = {
        responsive: true,
        plugins: {
            legend: {
                position: 'top' as const,
            },
            title: {
                display: true,
                text: t('analysisPanel.incomesAndExpensesTransactionsChartTitle') + ' ' + startDateString + " - " + endDateString,
            },
        },
        scales: {
            x: {
                title: {
                    display: true,
                    text: t('analysisPanel.incomesAndExpensesTransactionsChartXAxis'),
                },
                stacked: true,
            },
            y: {
                title: {
                    display: true,
                    text: t('analysisPanel.incomesAndExpensesTransactionsChartYAxis'),
                },
                stacked: true,
            },
        }
    };


    const handleAnalysisDataProccessPerDay = () => {
        let config = {
            method: 'POST',
            url: API_URL + '/analysis/transactions',
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
                const combinedKeys = Object.keys(response.data.transactionsExpensesPerDay).concat(Object.keys(response.data.transactionsIncomesPerDay));
                const labels = [...new Set(combinedKeys)];

                labels.sort((a, b) => {
                    // Przekonwertuj stringi na obiekty Date
                    const dateA = new Date(a);
                    const dateB = new Date(b);

                    // Porównaj daty
                    if (dateA < dateB) return -1;
                    if (dateA > dateB) return 1;
                    return 0;
                });

                const chartData = {
                    labels,
                    datasets: [
                        {
                            fill: true,
                            label: t('analysisPanel.expenses'),
                            data: labels.map(date => response.data.transactionsExpensesPerDay[date].reduce((total1, transaction1) => total1 - transaction1.amount, 0)),
                            borderColor: '#e15c5c',
                            backgroundColor: '#eb6666',
                        },
                        {
                            fill: true,
                            label: t('analysisPanel.incomes'),
                            data: labels.map(date => response.data.transactionsIncomesPerDay[date].reduce((total, transaction) => total + transaction.amount, 0)),
                            borderColor: '#7ed0a8',
                            backgroundColor: '#8dd7b3',
                        },
                    ],
                };
                setTransactionData(chartData);
                setDateWithBiggestExpense(response.data.dateWithBiggestExpenseRate);
                setRows(response.data.transactions);

            }).catch((error) => {
                setGenerated(false);
            })
    }

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
                        {t('analysisPanel.balanceDescription2')}
                    </div>
                </div>

            </div>
            <div className="content-column"> {/* Druga kolumna 75% szerokości */}
                {/* Twój zawartość dla drugiej kolumny */}
                <Box sx={boxStyle}>
                    {!generated ?
                        <>
                            <img src={emptyAnalysis} alt='empty' style={{ width: '35%' }} />
                            <div className="font-link" style={{ ...headerTextStyle, marginBottom: '5%', marginTop: '5%', display: 'block' }}>
                                {t('analysisPanel.noResultForData')}
                            </div>
                        </> :
                        <>
                            <div className="rectangle">
                                <div className="square-container">
                                    <div className="rectangle1"> <Bar options={options} data={dataSet} /> </div>
                                    <div className="rectangle1">
                                        <div className="font-link" style={{
                                            ...headerTextStyle, margin: '5%', display: 'block', padding: '10%',
                                            fontSize: '1.2rem', paddingLeft: '10%', backgroundColor: '#c5f2dc', borderRadius: '10px'
                                        }}>
                                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                                                <div style={{ fontWeight: 'bold' }}>
                                                    {t('analysisPanel.dayWithBiggestExpense')}
                                                </div>
                                                <div style={{ textAlign: 'right', paddingTop: '10%' }}>
                                                    {dateWithBiggestExpense}
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <div className="rectangle3"> <Bar options={transactionsOptions} data={transactionData} /> </div>
                            </div>

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
                                                        {(row.transactionType === 'INCOME' || row.transactionType === "INCOME") ? (
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
};

export default ExpensesAndIncomesAnalysisPanel;