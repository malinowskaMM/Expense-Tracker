import * as React from 'react';
import { TableHead, Box, FormControl, List, ListItem, TextField, MenuItem, Select, RadioGroup, FormControlLabel, Radio, Table, Dialog, DialogTitle, Snackbar, Alert, DialogContent, DialogActions, TableBody, TableCell, TableContainer, TableFooter, TablePagination, TableRow, Paper, IconButton, styled, Button } from '@mui/material';
import { useTranslation } from "react-i18next";
import axios from 'axios';
import { API_URL } from '../../consts';
import { useNavigate } from 'react-router-dom';
import { TransactionResponseDto } from '../../types/TransactionResponseDto';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faTrash } from '@fortawesome/free-solid-svg-icons';
import { faPen } from '@fortawesome/free-solid-svg-icons';
import { TransactionChangeRequestDto } from '../../types/TransactionChangeRequestDto';
import { set, useForm } from 'react-hook-form';
import dayjs, { Dayjs } from 'dayjs';
import { DemoContainer } from '@mui/x-date-pickers/internals/demo';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { CategoryResponseDto } from '../../types/CategoryResponseDto';

interface CustomPaginationCalendarTableProps {
    key: any;
    dateValue: string;
    reloadComponent: boolean;
}

const CustomPaginationCalendarTable: React.FC<CustomPaginationCalendarTableProps> = ({ dateValue, reloadComponent }) => {
    const [page, setPage] = React.useState(0);
    const [rowsPerPage, setRowsPerPage] = React.useState(5);
    const navigate = useNavigate();
    const [rows, setRows] = React.useState<TransactionResponseDto[]>([]);
    const { t, i18n } = useTranslation();
    const [open, setOpen] = React.useState(false);
    const [selectedRowId, setSelectedRowId] = React.useState<number>();
    const [openDeleteTransactionDialog, setOpenDeleteTransactionDialog] = React.useState(false);
    const [openDeleteTransactionFailedAlert, setOpenDeleteTransactionFailedAlert] = React.useState(false);
    const [openDeleteTransactionSuccessAlert, setOpenDeleteTransactionSuccessAlert] = React.useState(false);
    const [openEditTransactionDialog, setOpenEditTransactionDialog] = React.useState(false);
    const [openEditTransactionFailedAlert, setOpenEditTransactionFailedAlert] = React.useState(false);
    const [openEditTransactionSuccessAlert, setOpenEditTransactionSuccessAlert] = React.useState(false);
    const { register, handleSubmit, reset } = useForm<TransactionChangeRequestDto>();
    const [categories, setCategories] = React.useState<CategoryResponseDto[]>([]);
    const [alertHeight, setAlertHeight] = React.useState(0);
    const [validationErrors, setValidationErrors] = React.useState<string[]>([]);

    const [selectedRowName, setSelectedRowName] = React.useState<string>();
    const [selectedRowCategoryName, setSelectedRowCategoryName] = React.useState<string>();
    const [selecetedRowCategoryColor, setSelecetedRowCategoryColor] = React.useState<string>();
    const [selecetedRowCategoryId, setSelecetedRowCategoryId] = React.useState<string>();
    const [selectedRowGroupName, setSelectedRowGroupName] = React.useState<string>();
    const [selectedRowIsCyclic, setSelectedRowIsCyclic] = React.useState<boolean>();
    const [selectedRowPeriod, setSelectedRowPeriod] = React.useState<number>();
    const [selectedRowPeriodUnit, setSelectedRowPeriodUnit] = React.useState<string>();
    const [selectedRowDate, setSelectedRowDate] = React.useState<string>('');
    const [selectedRowAmount, setSelectedRowAmount] = React.useState<number>();
    const [selectedRowAccountId, setSelectedRowAccountId] = React.useState<number>();
    const [selectedRowType, setSelectedRowType] = React.useState<string>();
    const [selectedRowSign, setSelectedRowSign] = React.useState<string>();
    const [selectedRowVersion, setSelectedRowVersion] = React.useState<number>();

    const [openStopRecurringTransactionFailedAlert, setOpenStopRecurringTransactionFailedAlert] = React.useState(false);
    const [openStopRecurringTransactionSuccessAlert, setOpenStopRecurringTransactionSuccessAlert] = React.useState(false);
    const [openRenewRecurringTransactionFailedAlert, setOpenRenewRecurringTransactionFailedAlert] = React.useState(false);
    const [openRenewRecurringTransactionSuccessAlert, setOpenRenewRecurringTransactionSuccessAlert] = React.useState(false);

    const [openFailedAlertByOutdatedData, setOpenFailedAlertByOutdatedData] = React.useState(false);

    let config = {
        method: 'GET',
        url: API_URL + '/transactions/account/' + localStorage.getItem("id") + '/byDate/' + dateValue,
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + localStorage.getItem('token')
        },
    };

    const featchData = async () => {
        axios.request(config)
            .then((response) => {
                setRows(response.data.transactions);
            }).catch((error) => {
            })
    }

    React.useEffect(() => {
        let configIn = {
            method: 'GET',
            url: API_URL + '/transactions/account/' + localStorage.getItem("id") + '/byDate/' + dateValue,
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            },
        };

        axios.request(configIn)
            .then((response) => {
                setRows(response.data.transactions);
            }).catch((error) => {
            })
    }, [dateValue, reloadComponent]);


    const handleStopRecurringTransaction = (transactionId: string) => {
        let config = {
            method: 'PATCH',
            url: API_URL + '/transactions/transaction/' + transactionId + '/stop-recurring',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            },
        };

        axios.request(config)
            .then((response) => {
                setOpenStopRecurringTransactionFailedAlert(false);
                setOpenFailedAlertByOutdatedData(false);
                setOpenStopRecurringTransactionSuccessAlert(true);
                featchData();
            }).catch((error) => {
                if (error.response && error.response.status === 422) {
                    setOpenFailedAlertByOutdatedData(true);
                    setOpenStopRecurringTransactionSuccessAlert(false);
                    setOpenStopRecurringTransactionFailedAlert(false);
                } else {
                    setOpenStopRecurringTransactionSuccessAlert(false);
                    setOpenFailedAlertByOutdatedData(false);
                    setOpenStopRecurringTransactionFailedAlert(true);
                }
            })
    }

    const handleRenewRecurringTransaction = (transactionId: string) => {
        let config = {
            method: 'PATCH',
            url: API_URL + '/transactions/transaction/' + transactionId + '/renew-recurring',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            },
        };

        axios.request(config)
            .then((response) => {
                setOpenRenewRecurringTransactionFailedAlert(false);
                setOpenFailedAlertByOutdatedData(false);
                setOpenRenewRecurringTransactionSuccessAlert(true);
                featchData();
            }).catch((error) => {
                if (error.response && error.response.status === 422) {
                    setOpenFailedAlertByOutdatedData(true);
                    setOpenRenewRecurringTransactionFailedAlert(false);
                    setOpenRenewRecurringTransactionSuccessAlert(false);
                } else {
                    setOpenRenewRecurringTransactionSuccessAlert(false);
                    setOpenFailedAlertByOutdatedData(false);
                    setOpenRenewRecurringTransactionFailedAlert(true);
                }
            })
    }

    const handleStopRecurringTransactionFailedAlertClose = (event?: React.SyntheticEvent, reason?: string) => {
        if (reason === 'clickaway') {
            return;
        }
        setOpenStopRecurringTransactionFailedAlert(false);
    }

    const handleStopRecurringTransactionSuccessAlertClose = (event?: React.SyntheticEvent, reason?: string) => {
        if (reason === 'clickaway') {
            return;
        }
        setOpenStopRecurringTransactionSuccessAlert(false);
    }

    const handleRenewRecurringTransactionFailedAlertClose = (event?: React.SyntheticEvent, reason?: string) => {
        if (reason === 'clickaway') {
            return;
        }
        setOpenRenewRecurringTransactionFailedAlert(false);
    }

    const handleRenewRecurringTransactionSuccessAlertClose = (event?: React.SyntheticEvent, reason?: string) => {
        if (reason === 'clickaway') {
            return;
        }
        setOpenRenewRecurringTransactionSuccessAlert(false);
    }

    const handleChangeFailedByOutdatedDataAlertClose = (event?: React.SyntheticEvent, reason?: string) => {
        if (reason === 'clickaway') { return; }
        setOpenFailedAlertByOutdatedData(false);
    };


    // Avoid a layout jump when reaching the last page with empty rows.
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

    const handleEditTransaction = (transactionId: string) => {
        setSelectedRowId(transactionId);
        let configGetTransaction = {
            method: 'GET',
            url: API_URL + '/transactions/transaction/' + transactionId,
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            },
        };

        axios.request(configGetTransaction)
            .then((response) => {
                setSelectedRowName(response.data.name);
                setSelectedRowCategoryName(response.data.categoryName);
                setSelecetedRowCategoryColor(response.data.categoryColor);
                setSelecetedRowCategoryId(response.data.categoryId);
                setSelectedRowGroupName(response.data.groupName);
                setSelectedRowIsCyclic(response.data.isCyclic);
                setSelectedRowPeriod(response.data.period);
                setSelectedRowPeriodUnit(response.data.periodUnit);
                setSelectedRowDate(response.data.date);
                setSelectedRowAmount(response.data.amount);
                setSelectedRowAccountId(response.data.accountId);
                setSelectedRowType(response.data.type);
                setSelectedRowSign(response.data.sign);
                setSelectedRowVersion(response.data.version);


            }).catch((error) => {
            })


        let categoryConfig = {
            method: 'GET',
            url: API_URL + '/groups/all/categories/account/' + localStorage.getItem("id"),
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            },
        };

        axios.request(categoryConfig)
            .then((response) => {
                setCategories(response.data.categories);
            }).catch((error) => {
            })

        setOpenEditTransactionDialog(true);
    }

    const handleEdit = () => {

        let requestData: TransactionChangeRequestDto = {
            name: selectedRowName,
            cycle: selectedRowIsCyclic ? 'CYCLE' : 'ONETIME',
            period: selectedRowPeriod,
            periodType: selectedRowPeriodUnit,
            type: selectedRowType,
            categoryId: selecetedRowCategoryId,
            date: selectedRowDate,
            value: selectedRowAmount,
            version: selectedRowVersion.toString()
        }

        let editTransactionConfig = {
            method: 'PATCH',
            url: API_URL + '/transactions/transaction/' + selectedRowId,
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('token'),
                'If-Match': selectedRowSign
            },
            data: requestData
        };

        axios.request(editTransactionConfig)
            .then((response) => {
                setSelectedRowName('');
                setSelectedRowIsCyclic(false);
                setSelectedRowPeriod(0);
                setSelectedRowPeriodUnit('');
                setSelectedRowType('');
                setSelecetedRowCategoryId('')
                setSelectedRowDate('');
                setSelectedRowAmount(0);

                setOpenEditTransactionDialog(false);
                setOpenEditTransactionSuccessAlert(true);
                setOpenEditTransactionFailedAlert(false);
                setOpenFailedAlertByOutdatedData(false);
                featchData();
            }).catch((error) => {
                if (error.response && error.response.status === 422) {
                    setOpenFailedAlertByOutdatedData(true);
                    setOpenEditTransactionSuccessAlert(false);
                    setOpenEditTransactionFailedAlert(false);
                    featchData();
                } else {
                    setOpenEditTransactionSuccessAlert(false);
                    setOpenFailedAlertByOutdatedData(false);
                    setOpenEditTransactionFailedAlert(true);
                    featchData();
                }
            })

    }

    const handleClose = () => {
        setOpen(false);
    };

    const handleDeleteTransaction = (transactionId: string) => {
        setSelectedRowId(transactionId);
        setOpenDeleteTransactionDialog(true);
    }

    const handleDeleteTransactionFailedAlertClose = (event?: React.SyntheticEvent, reason?: string) => {
        if (reason === 'clickaway') {
            return;
        }
        setOpenDeleteTransactionFailedAlert(false);
    }

    const handleDeleteTransactionSuccessAlertClose = (event?: React.SyntheticEvent, reason?: string) => {
        if (reason === 'clickaway') {
            return;
        }
        setOpenDeleteTransactionSuccessAlert(false);
    }

    const handleEditChosenTransaction = () => {
        handleEdit();
    }

    const handleEditTransactionFailedAlertClose = (event?: React.SyntheticEvent, reason?: string) => {
        if (reason === 'clickaway') {
            return;
        }
        setOpenEditTransactionFailedAlert(false);
    }

    const handleEditTransactionSuccessAlertClose = (event?: React.SyntheticEvent, reason?: string) => {
        if (reason === 'clickaway') {
            return;
        }
        setOpenEditTransactionSuccessAlert(false);
    }

    const handleDeleteChosenTransaction = () => {
        let config = {
            method: 'DELETE',
            url: API_URL + '/transactions/transaction/' + selectedRowId,
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            },
        };

        const deleteData = async () => {
            axios.request(config)
                .then((response) => {
                    setOpenDeleteTransactionDialog(false);
                    setOpenDeleteTransactionSuccessAlert(true);
                    setOpenDeleteTransactionFailedAlert(false);
                    featchData();
                }).catch((error) => {
                    setOpenDeleteTransactionSuccessAlert(false);
                    setOpenDeleteTransactionFailedAlert(true);
                    featchData();
                })
        }
        deleteData();
    }

    const onSubmit = handleSubmit(() => {
    }
    );

    const handleSelectedRowTypeChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setSelectedRowType(event.target.value);
    };

    const handleSelectedRowNameChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setSelectedRowName(event.target.value);
        validateName(event.target.value);
    };

    const handleSelectedRowCyclicChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setSelectedRowIsCyclic(event.target.value === 'CYCLE');
        validateCycle(event.target.value);
    }

    const handleSelectedRowPeriodChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setSelectedRowPeriod(parseInt(event.target.value));
        validatePeriod(event.target.value);
    };

    const handleSelectedRowPeriodUnitChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setSelectedRowPeriodUnit(event.target.value);
        validatePeriodType(event.target.value);
    };

    const handleSelectedRowAmountChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setSelectedRowAmount(parseInt(event.target.value));
        validateAmount(parseInt(event.target.value));
    };

    const handleSelectedRowCategoryNameChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setSelectedRowCategoryName(event.target.value);

    };

    const validateCategory = (category: string) => {
        const isValid = category !== '';
        updateValidationErrors('category', isValid);
        return isValid;
    }

    const validateName = (name: string) => {
        const isValid = name.length > 3;
        updateValidationErrors('name', isValid);
        return isValid;
    }

    const validateCycle = (cycle: string) => {
        const isValid = (cycle !== '' && cycle !== 'ONETIME') || cycle === 'ONETIME';
        updateValidationErrors('cycle', isValid);
        return isValid;
    }

    const validatePeriod = (period: string) => {
        const isValid = period !== '';
        updateValidationErrors('period', isValid);
        return isValid;
    }

    const validatePeriodType = (periodType: string) => {
        const isValid = (periodType !== '' && selectedRowIsCyclic === true) || selectedRowIsCyclic === false;
        updateValidationErrors('periodType', isValid);
        return isValid;
    }

    const validateAmount = (amount: number) => {
        const isValid = amount > 0;
        updateValidationErrors('amount', isValid);
        return isValid;
    }

    const validateDate = (date: Dayjs | null) => {
        const isValid = date !== null;
        updateValidationErrors('date', isValid);
        return isValid;
    }

    const updateValidationErrors = (field: string, isValid: boolean) => {
        setValidationErrors((prevErrors) => {
            const updatedErrors = { ...prevErrors };
            if (!isValid) {
                switch (field) {
                    case 'category':
                        updatedErrors[field] = t('calendarPanel.categoryValidation');
                        break;
                    case 'name':
                        updatedErrors[field] = t('calendarPanel.nameValidation');
                        break;
                    case 'cycle':
                        updatedErrors[field] = t('calendarPanel.cycleValidation');
                        break;
                    case 'period':
                        updatedErrors[field] = t('calendarPanel.periodValidation');
                        break;
                    case 'periodType':
                        updatedErrors[field] = t('calendarPanel.periodTypeValidation');
                        break;
                    case 'amount':
                        updatedErrors[field] = t('calendarPanel.amountValidation');
                        break;
                    case 'date':
                        updatedErrors[field] = t('calendarPanel.dateValidation');
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

    const handleAlertLoad = (event: React.ChangeEvent<HTMLInputElement>) => {
        setAlertHeight(event.target.clientHeight);
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

    const StyledEditTransactionBox = styled(StyledBox)({
        display: 'flex',
        backgroundColor: '#4CAF50',
        color: 'white',
        padding: '13%',
    })

    const StyledDeleteTransactionBox = styled(StyledBox)({
        display: 'flex',
        backgroundColor: '#FF3030',
        color: 'white',
        padding: '13%',
    })

    const dialoButtonTextStyle = {
        textAlign: 'center' as const,
        backgroundColor: 'rgba(131, 225, 180, 0.7)',
        borderRadius: '5vh',
        color: '#1B2024',
        padding: '1%',
        fontWeight: '100',
        fontFamily: [
            "Inter",
            'sans-serif',
        ].join(','),
        width: '150px',
        height: '30px',
    }

    return (
        <>
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
                                    {t('calendarPanel.amount')}
                                </div>
                            </TableCell>
                            <TableCell>
                                <div className="font-link-table-header">
                                    {t('calendarPanel.options')}
                                </div>
                            </TableCell>
                            <TableCell>
                            </TableCell>
                            <TableCell>
                            </TableCell>
                            <TableCell>
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
                                    {row.amount}
                                </TableCell>
                                <TableCell align="left">
                                    <IconButton onClick={() => handleEditTransaction(row.id)}>
                                        <StyledEditTransactionBox>
                                            <FontAwesomeIcon icon={faPen} style={{ paddingRight: '5px' }} />
                                            {t('calendarPanel.edit')}
                                        </StyledEditTransactionBox>
                                    </IconButton>
                                </TableCell>
                                <TableCell align="left">
                                    <IconButton onClick={() => handleDeleteTransaction(row.id)}>
                                        <StyledDeleteTransactionBox>
                                            <FontAwesomeIcon icon={faTrash} style={{ paddingRight: '5px' }} />
                                            {t('calendarPanel.delete')}
                                        </StyledDeleteTransactionBox>
                                    </IconButton>
                                </TableCell>
                                <TableCell align="left">
                                    {row.isCyclic === true && row.endDate == null ? (
                                        <IconButton onClick={() => handleStopRecurringTransaction(row.id)}>
                                            <StyledDeleteTransactionBox>
                                                <div>{t('calendarPanel.stop')}</div>
                                            </StyledDeleteTransactionBox>
                                        </IconButton>
                                    ) : (
                                        <></>
                                    )}
                                </TableCell>
                                <TableCell align="left">
                                    {row.isCyclic === true && row.endDate != null ? (
                                        <IconButton onClick={() => handleRenewRecurringTransaction(row.id)}>
                                            <StyledEditTransactionBox>
                                                <div>{t('calendarPanel.renew')}</div>
                                            </StyledEditTransactionBox>
                                        </IconButton>
                                    ) : (
                                        <></>
                                    )}
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
            <Dialog open={open} onClose={handleClose} aria-labelledby="form-dialog-title">
                <DialogTitle id="form-dialog-title">{t('calendarPanel.edit')}</DialogTitle>
                <DialogContent>

                </DialogContent>
                <DialogActions>
                    <Button onClick={handleClose} color="primary">
                        {t('calendarPanel.cancel')}
                    </Button>
                    <Button onClick={handleEdit} color="primary">
                        {t('calendarPanel.edit')}
                    </Button>
                </DialogActions>
            </Dialog>




            <Dialog
                open={openDeleteTransactionDialog}
                onClose={() => setOpenDeleteTransactionDialog(false)}
                PaperProps={{
                    style: {
                        minWidth: '50%',
                        borderRadius: '40px',
                        padding: '2%',
                        backgroundColor: '#bef4da',
                        boxShadow: 'initial'
                    },
                }}
            >
                <DialogTitle id="alert-dialog-title">
                    {t('calendarPanel.deleteTransactionTitle')}
                </DialogTitle>
                <DialogContent>
                    <div className="font-link">
                        {t('calendarPanel.deleteTransaction')}
                    </div>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setOpenDeleteTransactionDialog(false)} color="primary" style={dialoButtonTextStyle}>
                        {t('calendarPanel.cancel')}
                    </Button>
                    <Button onClick={handleDeleteChosenTransaction} color="primary" autoFocus style={dialoButtonTextStyle}>
                        {t('calendarPanel.delete')}
                    </Button>
                </DialogActions>
            </Dialog>



            <Snackbar open={openDeleteTransactionFailedAlert} autoHideDuration={6000} onClose={handleDeleteTransactionFailedAlertClose}>
                <Alert severity="error" sx={{ width: '100%' }} onClose={handleDeleteTransactionFailedAlertClose}>
                    {t('calendarPanel.deleteTransactionFailed')}
                </Alert>
            </Snackbar>

            <Snackbar open={openDeleteTransactionSuccessAlert} autoHideDuration={6000} onClose={handleDeleteTransactionSuccessAlertClose}>
                <Alert severity="success" sx={{ width: '100%' }} onClose={handleDeleteTransactionSuccessAlertClose}>
                    {t('calendarPanel.deleteTransactionSuccess')}
                </Alert>
            </Snackbar>


            <Dialog
                open={openEditTransactionDialog}
                onClose={() => setOpenEditTransactionDialog(false)}
                PaperProps={{
                    style: {
                        minWidth: '50%',
                        borderRadius: '40px',
                        padding: '2%',
                        backgroundColor: '#bef4da',
                        boxShadow: 'initial'
                    },
                }}
            >
                <DialogTitle id="alert-dialog-title">
                    {t('calendarPanel.editTransactionTitle')}
                </DialogTitle>
                <DialogContent>
                    <div className="font-link">
                        {t('calendarPanel.editTransaction')}
                    </div>
                </DialogContent>
                <FormControl>
                    <List>
                        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', flexDirection: 'column' }}>
                            <Box component="form" onSubmit={onSubmit}>
                                <ListItem>
                                    <div className="font-link">
                                        {t('calendarPanel.kind')}*
                                    </div>
                                    <div className="paddingConatiner">
                                        <Select
                                            disabled
                                            row
                                            {...register("type")}
                                            value={selectedRowType}
                                            onChange={handleSelectedRowTypeChange}>
                                            <MenuItem value={'INCOME'}>
                                                <StyledIncomeTypeBox>{t('calendarPanel.income')}</StyledIncomeTypeBox>
                                            </MenuItem>
                                            <MenuItem value={'EXPENSE'}>
                                                <StyledExpenseTypeBox>{t('calendarPanel.expense')}</StyledExpenseTypeBox>
                                            </MenuItem>

                                        </Select>
                                    </div>
                                </ListItem>


                                <ListItem>
                                    <div className="font-link">
                                        {t('calendarPanel.name')}*
                                    </div>
                                    <div className="paddingConatiner">
                                        <TextField
                                            required
                                            {...register("name")}
                                            id="outlined-required-email"
                                            style={{ backgroundColor: 'white', width: '100%' }}
                                            variant='standard'
                                            value={selectedRowName}
                                            onChange={handleSelectedRowNameChange}
                                        />
                                    </div>
                                </ListItem>


                                <ListItem style={{ display: 'flex' }}>
                                    <div className="font-link">
                                        {t('calendarPanel.incomeCycle')}*
                                    </div>
                                    <div className="paddingConatiner">
                                        <RadioGroup
                                            row
                                            {...register("cycle")}
                                            value={selectedRowIsCyclic ? 'CYCLE' : 'ONETIME'}
                                            onChange={handleSelectedRowCyclicChange}>
                                            <FormControlLabel value={'ONETIME'} control={<Radio />} label={t('calendarPanel.oneTimeTransaction')} />
                                            <FormControlLabel value={'CYCLE'} control={<Radio />} label={t('calendarPanel.recurringTransaction')} />
                                        </RadioGroup>
                                    </div>
                                </ListItem>


                                <ListItem style={{ display: 'flex' }}>
                                    <div className="font-link">
                                        {t('calendarPanel.period')}*
                                    </div>
                                    <div>
                                        <RadioGroup
                                            row
                                            {...register("periodType")}
                                            value={selectedRowIsCyclic === false ? "" : selectedRowPeriodUnit}
                                            onChange={handleSelectedRowPeriodUnitChange}
                                        >
                                            <div className="paddingConatiner">
                                                <TextField
                                                    required
                                                    {...register("period")}
                                                    id="outlined-required-email"
                                                    style={{ backgroundColor: 'white', width: '100%' }}
                                                    variant='standard'
                                                    value={selectedRowIsCyclic === false ? "" : selectedRowPeriod}
                                                    onChange={handleSelectedRowPeriodChange}
                                                    disabled={selectedRowIsCyclic === false}
                                                />
                                            </div>
                                            <div className="paddingConatiner">
                                                <FormControlLabel value={'DAY'} control={<Radio disabled={selectedRowIsCyclic === false} />} label={t('calendarPanel.days')} />
                                                <FormControlLabel value={'MONTH'} control={<Radio disabled={selectedRowIsCyclic === false} />} label={t('calendarPanel.months')} />
                                                <FormControlLabel value={'YEAR'} control={<Radio disabled={selectedRowIsCyclic === false} />} label={t('calendarPanel.years')} />
                                            </div>
                                        </RadioGroup>
                                    </div>
                                </ListItem>


                                <ListItem style={{ display: 'flex' }}>
                                    <div className="font-link">
                                        {t('calendarPanel.category')}*
                                    </div>
                                    <div className="paddingConatiner">
                                        <Select
                                            {...register("categoryId")}
                                            labelId="demo-simple-select-label"
                                            id="demo-simple-select"
                                            value={selecetedRowCategoryId}
                                            onChange={handleSelectedRowCategoryNameChange}
                                        >
                                            {categories.map((item) => (
                                                <MenuItem key={item.id} value={item.id}>
                                                    {item.name + " " + item.groupName}
                                                </MenuItem>
                                            ))}
                                        </Select>
                                    </div>
                                </ListItem>


                                <ListItem style={{ display: 'flex' }}>
                                    <div className="font-link">
                                        {t('calendarPanel.date')}*
                                    </div>
                                    <div className="paddingConatiner">
                                        <LocalizationProvider dateAdapter={AdapterDayjs}>
                                            <DemoContainer components={['DatePicker']}>
                                                <DatePicker
                                                    value={dayjs(new Date(selectedRowDate))}
                                                    onChange={(newDate) => {
                                                        validateDate(newDate);
                                                        setSelectedRowDate(newDate.toDate().toISOString().split('T')[0]);
                                                    }
                                                    }
                                                />
                                            </DemoContainer>
                                        </LocalizationProvider>
                                    </div>
                                </ListItem>


                                <ListItem>
                                    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', flexDirection: 'row' }}>
                                        <div className="font-link">
                                            {t('calendarPanel.amount')}*
                                        </div>
                                        <div className="paddingConatiner">
                                            <TextField
                                                required
                                                {...register("value")}
                                                id="outlined-required-email"
                                                style={{ backgroundColor: 'white', width: '100%' }}
                                                variant='standard'
                                                value={selectedRowAmount}
                                                onChange={handleSelectedRowAmountChange}
                                            />
                                        </div>
                                    </div>
                                </ListItem>
                            </Box>
                        </div>
                    </List>
                </FormControl>

                <DialogActions>
                    <Button onClick={() => setOpenEditTransactionDialog(false)} color="primary" style={dialoButtonTextStyle}>
                        {t('calendarPanel.cancel')}
                    </Button>
                    <Button onClick={handleEditChosenTransaction} color="primary" autoFocus style={dialoButtonTextStyle}>
                        {t('calendarPanel.edit')}
                    </Button>
                </DialogActions>
            </Dialog>

            <Snackbar open={openEditTransactionFailedAlert} autoHideDuration={6000} onClose={handleEditTransactionFailedAlertClose}>
                <Alert severity="error" sx={{ width: '100%' }} onClose={handleEditTransactionFailedAlertClose}>
                    {t('calendarPanel.editTransactionFailed')}
                </Alert>
            </Snackbar>

            <Snackbar open={openEditTransactionSuccessAlert} autoHideDuration={6000} onClose={handleEditTransactionSuccessAlertClose}>
                <Alert severity="success" sx={{ width: '100%' }} onClose={handleEditTransactionSuccessAlertClose}>
                    {t('calendarPanel.editTransactionSuccess')}
                </Alert>
            </Snackbar>

            <Snackbar open={openStopRecurringTransactionFailedAlert} autoHideDuration={6000} onClose={handleStopRecurringTransactionFailedAlertClose}>
                <Alert severity="error" sx={{ width: '100%' }} onClose={handleStopRecurringTransactionFailedAlertClose}>
                    {t('calendarPanel.stopRecurringTransactionFailed')}
                </Alert>
            </Snackbar>

            <Snackbar open={openStopRecurringTransactionSuccessAlert} autoHideDuration={6000} onClose={handleStopRecurringTransactionSuccessAlertClose}>
                <Alert severity="success" sx={{ width: '100%' }} onClose={handleStopRecurringTransactionSuccessAlertClose}>
                    {t('calendarPanel.stopRecurringTransactionSuccess')}
                </Alert>
            </Snackbar>

            <Snackbar open={openRenewRecurringTransactionFailedAlert} autoHideDuration={6000} onClose={handleRenewRecurringTransactionFailedAlertClose}>
                <Alert severity="error" sx={{ width: '100%' }} onClose={handleRenewRecurringTransactionFailedAlertClose}>
                    {t('calendarPanel.renewRecurringTransactionFailed')}
                </Alert>
            </Snackbar>

            <Snackbar open={openRenewRecurringTransactionSuccessAlert} autoHideDuration={6000} onClose={handleRenewRecurringTransactionSuccessAlertClose}>
                <Alert severity="success" sx={{ width: '100%' }} onClose={handleRenewRecurringTransactionSuccessAlertClose}>
                    {t('calendarPanel.renewRecurringTransactionSuccess')}
                </Alert>
            </Snackbar>

            <Snackbar open={openFailedAlertByOutdatedData} autoHideDuration={6000} onClose={handleChangeFailedByOutdatedDataAlertClose}>
                <Alert severity="error" sx={{ width: '100%' }} onClose={handleChangeFailedByOutdatedDataAlertClose}>
                    {t('common.optimisticLockError')}
                </Alert>
            </Snackbar>
        </>
    );
}

export default CustomPaginationCalendarTable;