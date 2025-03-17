import * as React from 'react';
import { useTheme } from '@mui/material/styles';
import { KeyboardArrowRight, KeyboardArrowLeft } from '@mui/icons-material';
import FirstPageIcon from '@mui/icons-material/FirstPage';
import LastPageIcon from '@mui/icons-material/LastPage';
import { TableHead, Button, styled, IconButton, Box, Table, TableBody, TableCell, TableContainer, TableFooter, AlertTitle, TablePagination, Snackbar, Alert, TableRow, Paper, Dialog, DialogActions, DialogContent, DialogTitle, DialogContentText, List, ListItem, Tooltip, Switch, Stack, Typography, TextField, Select, MenuItem } from '@mui/material';
import { useTranslation } from "react-i18next";
import axios from 'axios';
import { API_URL } from '../../consts';
import { useNavigate } from 'react-router-dom';
import { AccountResponseDto } from '../../types/AccountResponseDto';
import { set } from 'react-hook-form';
import { AccountResetPasswordRequestDto } from '../../types/AccountResetPasswordRequestDto';
import { API_CALLBACK_ROUTE_RESET_PASSWORD } from '../../consts';
import { AccountEmailChangeRequestDto } from '../../types/AccountEmailChangeRequestDto';
import { AccountLanguageChangeRequestDto } from '../../types/AccountLanguageChangeRequestDto';
import { API_CALLBACK_ROUTE_CHANGE_EMAIL } from '../../consts';

interface TablePaginationActionsProps {
    count: number;
    page: number;
    rowsPerPage: number;
    onPageChange: (
        event: React.MouseEvent<HTMLButtonElement>,
        newPage: number,
    ) => void;
}

function TablePaginationActions(props: TablePaginationActionsProps) {
    const theme = useTheme();
    const { count, page, rowsPerPage, onPageChange } = props;

    const handleFirstPageButtonClick = (
        event: React.MouseEvent<HTMLButtonElement>,
    ) => {
        onPageChange(event, 0);
    };

    const handleBackButtonClick = (event: React.MouseEvent<HTMLButtonElement>) => {
        onPageChange(event, page - 1);
    };

    const handleNextButtonClick = (event: React.MouseEvent<HTMLButtonElement>) => {
        onPageChange(event, page + 1);
    };

    const handleLastPageButtonClick = (event: React.MouseEvent<HTMLButtonElement>) => {
        onPageChange(event, Math.max(0, Math.ceil(count / rowsPerPage) - 1));
    };

    return (
        <Box sx={{ flexShrink: 0, ml: 2.5 }}>
            <IconButton
                onClick={handleFirstPageButtonClick}
                disabled={page === 0}
                aria-label="first page"
            >
                {theme.direction === 'rtl' ? <LastPageIcon /> : <FirstPageIcon />}
            </IconButton>
            <IconButton
                onClick={handleBackButtonClick}
                disabled={page === 0}
                aria-label="previous page"
            >
                {theme.direction === 'rtl' ? <KeyboardArrowRight /> : <KeyboardArrowLeft />}
            </IconButton>
            <IconButton
                onClick={handleNextButtonClick}
                disabled={page >= Math.ceil(count / rowsPerPage) - 1}
                aria-label="next page"
            >
                {theme.direction === 'rtl' ? <KeyboardArrowLeft /> : <KeyboardArrowRight />}
            </IconButton>
            <IconButton
                onClick={handleLastPageButtonClick}
                disabled={page >= Math.ceil(count / rowsPerPage) - 1}
                aria-label="last page"
            >
                {theme.direction === 'rtl' ? <FirstPageIcon /> : <LastPageIcon />}
            </IconButton>
        </Box>
    );
}



export default function CustomPaginationActionsTable() {
    const [page, setPage] = React.useState(0);
    const [rowsPerPage, setRowsPerPage] = React.useState(5);
    const navigate = useNavigate();
    const [rows, setRows] = React.useState<AccountResponseDto[]>([]);
    const { t, i18n } = useTranslation();
    const [openUserDetails, setOpenUserDetails] = React.useState(false);
    const [selectedRowId, setSelectedRowId] = React.useState<number>(-2137);
    const [accountEmail, setAccountEmail] = React.useState('');
    const [accountRegistrationDate, setAccountRegistrationDate] = React.useState('');
    const [accountLastValidLoginDate, setAccountLastValidLoginDate] = React.useState('');
    const [accountLastInvalidLoginDate, setAccountLastInvalidLoginDate] = React.useState('');
    const [accountEnable, setAccountEnable] = React.useState(false);
    const [accountActive, setAccountActive] = React.useState(false);
    const [accountRole, setAccountRole] = React.useState('');
    const [accountLanguage, setAccountLanguage] = React.useState(localStorage.getItem('i18nextLng') || 'enUS');
    const [accountSign, setAccountSign] = React.useState('');
    const pl = require("../../assets/pl.svg").default;
    const gb = require("../../assets/gb.svg").default;
    const [openEditAccountEmail, setOpenEditAccountEmail] = React.useState(false);
    const [accountEmailConfirmation, setAccountEmailConfirmation] = React.useState('');
    const [openResetAccountPassword, setOpenResetAccountPassword] = React.useState(false);
    const [openChangeAccountLanguage, setOpenChangeAccountLanguage] = React.useState(false);
    const [openChangeAccountEnable, setOpenChangeAccountEnable] = React.useState(false);
    const [accountEmailDialog, setAccountEmailDialog] = React.useState('');
    const [accountLanguageDialog, setAccountLanguageDialog] = React.useState('');
    const [accountEnableDialog, setAccountEnableDialog] = React.useState(false);
    const token = "Bearer " + localStorage.getItem("token");
    const [openResetAccountPasswordSucessAlert, setOpenResetAccountPasswordSucessAlert] = React.useState(false);
    const [openResetAccountPasswordFailedAlert, setOpenResetAccountPasswordFailedAlert] = React.useState(false);
    const [openChangeEnableAccountSucessAlert, setOpenChangeEnableAccountSucessAlert] = React.useState(false);
    const [openChangeEnableAccountFailedAlert, setOpenChangeEnableAccountFailedAlert] = React.useState(false);
    const [accountVersion, setAccountVersion] = React.useState(-1);
    const [openChangeLanguageAccountSucessAlert, setOpenChangeLanguageAccountSucessAlert] = React.useState(false);
    const [openChangeLanguageAccountFailedAlert, setOpenChangeLanguageAccountFailedAlert] = React.useState(false);
    const [openChangeEmailAccountSucessAlert, setOpenChangeEmailAccountSucessAlert] = React.useState(false);
    const [openChangeEmailAccountFailedAlert, setOpenChangeEmailAccountFailedAlert] = React.useState(false);
    const [validationErrors, setValidationErrors] = React.useState<string[]>([]);
    const [alertHeight, setAlertHeight] = React.useState(0);
    const [isSaveEditEmailButtonDisabled, setIsSaveEditEmailButtonDisabled] = React.useState(true);
    const [openFailedAlertByOutdatedData, setOpenFailedAlertByOutdatedData] = React.useState(false);

    React.useEffect(() => {

        let config = {
            method: 'GET',
            url: API_URL + '/accounts',
            headers: {
                'Authorization': token
            },
        };

        const featchData = async () => {
            axios.request(config)
                .then((response) => {
                    setRows(response.data.accounts);
                }).catch((error) => {
                })

        }
        featchData();

    }, []);


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

    const handleChangeFailedByOutdatedDataAlertClose = (event?: React.SyntheticEvent, reason?: string) => {
        if (reason === 'clickaway') { return; }
        setOpenFailedAlertByOutdatedData(false);
    };


    const handleAccountDetails = (accountId: number | undefined) => {
        console.log(localStorage.getItem("id"), selectedRowId)
        setSelectedRowId(accountId);
        setOpenUserDetails(true);


        let config = {
            method: 'GET',
            url: API_URL + '/accounts/account/' + accountId,
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            },
        };

        const featchAccountData = async () => {
            axios.request(config)
                .then((response) => {
                    setAccountEmail(response.data.email);
                    setAccountRole(response.data.role);
                    setAccountEnable(response.data.enable);
                    setAccountActive(response.data.active);
                    setAccountLanguage(response.data.language);
                    setAccountRegistrationDate(response.data.registerData);
                    setAccountLastValidLoginDate(response.data.lastValidLoginDate);
                    setAccountLastInvalidLoginDate(response.data.lastInvalidLoginDate);
                    setAccountSign(response.data.sign);
                    setAccountVersion(response.data.version);
                })
        }

        featchAccountData();

    }

    const handleAlertLoad = (event: React.ChangeEvent<HTMLInputElement>) => {
        setAlertHeight(event.target.clientHeight);
        console.log(event.target.clientHeight);
    };

    const handleCloseUserDetails = () => {
        setOpenUserDetails(false);
    }

    const handleSwitchAccountActiveChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setAccountActive(event.target.checked);
    }

    const handleEditAccountEmailChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setAccountEmailDialog(event.target.value);
        validateEmail(event.target.value);
    }

    const validateEmail = (email: string) => {
        const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        const isValid = re.test(email);
        updateValidationErrors('email', isValid);
        return isValid;
    }

    const validateRepeatEmail = (email: string, repeatEmail: string) => {
        const isValid = email.localeCompare(repeatEmail);
        updateValidationErrors('repeated email', isValid === 0);
        return isValid;
    }

    const handleEditAccountEmail = () => {
        let data: AccountEmailChangeRequestDto = {
            newEmail: accountEmailDialog,
            repeatedNewEmail: accountEmailConfirmation,
            version: accountVersion,
            callbackRoute: API_CALLBACK_ROUTE_CHANGE_EMAIL
        }

        let config = {
            method: 'PATCH',
            url: API_URL + '/accounts/account/' + selectedRowId + '/email',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('token'),
                'If-Match': accountSign
            },
            data: JSON.stringify(data),
        };

        axios.request(config).then((response) => {
            setAccountEmailDialog('');
            setAccountEmailConfirmation('');

            setOpenEditAccountEmail(false);
            setOpenChangeEmailAccountSucessAlert(true);
            setOpenChangeEmailAccountFailedAlert(false);
            setOpenFailedAlertByOutdatedData(false);
            handleAccountDetails(selectedRowId);
        }).catch((error) => {

            if (error.response && error.response.status === 422) {
                setOpenFailedAlertByOutdatedData(true);
                setOpenChangeEmailAccountSucessAlert(false);
                setOpenChangeEmailAccountFailedAlert(false);
            } else {
                setOpenChangeEmailAccountFailedAlert(true);
                setOpenChangeEmailAccountSucessAlert(false);
                setOpenFailedAlertByOutdatedData(false);
            }
        });
        setOpenEditAccountEmail(false);
    }

    const handleCloseEditAccountEmail = () => {
        setOpenEditAccountEmail(false);
    }

    const handleEditAccountEmailConfirmationChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const newRepeatEmail = event.target.value.trim();
        setAccountEmailConfirmation(newRepeatEmail);
        validateRepeatEmail(accountEmailDialog.trim(), newRepeatEmail);
    }

    const handleResetAccountPassword = () => {
        let data: AccountResetPasswordRequestDto = {
            accountId: selectedRowId || '0',
            callbackRoute: API_CALLBACK_ROUTE_RESET_PASSWORD,
        };

        let config = {
            method: 'POST',
            url: API_URL + '/auth/account/authenticate/reset-password',
            headers: {
                'Content-Type': 'application/json',
            },
            data: JSON.stringify(data),
        };

        axios.request(config).then((response) => {
            setOpenResetAccountPassword(false);
            setOpenResetAccountPasswordSucessAlert(true);
        }).catch((error) => {
            setOpenResetAccountPasswordFailedAlert(true);
        });
    }

    const handleCloseResetAccountPassword = () => {
        setOpenResetAccountPassword(false);
    }

    const handleAccountLanguageChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setAccountLanguageDialog(event.target.value);
    }

    const handleChangeAccountLanguage = () => {
        let data: AccountLanguageChangeRequestDto = {
            language: accountLanguageDialog,
            version: accountVersion
        }

        let config = {
            method: 'PATCH',
            url: API_URL + '/accounts/account/' + selectedRowId + '/language',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('token'),
                'If-Match': accountSign
            },
            data: JSON.stringify(data),
        };

        axios.request(config).then((response) => {
            setAccountLanguageDialog('');

            setOpenChangeAccountLanguage(false);
            setOpenChangeLanguageAccountSucessAlert(true);
            setOpenChangeLanguageAccountFailedAlert(false);
            setOpenFailedAlertByOutdatedData(false);
            handleAccountDetails(selectedRowId);
        }).catch((error) => {
            if (error.response && error.response.status === 422) {
                setOpenFailedAlertByOutdatedData(true);
                setOpenChangeLanguageAccountSucessAlert(false);
                setOpenChangeLanguageAccountFailedAlert(false);
            } else {
                setOpenChangeLanguageAccountFailedAlert(true);
                setOpenChangeLanguageAccountSucessAlert(false);
                setOpenFailedAlertByOutdatedData(false);
            }
        });

        setOpenChangeAccountLanguage(false);
    }

    const handleCloseChangeAccountLanguage = () => {
        setOpenChangeAccountLanguage(false);
    }

    const handleAccountEnableChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setAccountEnableDialog(event.target.value === "true" ? true : false);
    }

    const handleChangeAccountEnable = () => {

        let enableConfig = {
            method: 'PATCH',
            url: API_URL + '/accounts/account/' + selectedRowId + '/enable',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            }
        };

        let disableConfig = {
            method: 'PATCH',
            url: API_URL + '/accounts/account/' + selectedRowId + '/disable',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            }
        };


        axios.request(accountEnableDialog === true ? enableConfig : disableConfig).then((response) => {
            setOpenChangeAccountEnable(false);
            setOpenChangeEnableAccountSucessAlert(true);
            setOpenChangeEnableAccountFailedAlert(false);
            setOpenFailedAlertByOutdatedData(false);
            handleAccountDetails(selectedRowId);
        }).catch((error) => {
            if (error.response && error.response.status === 422) {
                setOpenFailedAlertByOutdatedData(true);
                setOpenChangeEnableAccountSucessAlert(false);
                setOpenChangeEnableAccountFailedAlert(false);
            } else {
            setOpenChangeEnableAccountFailedAlert(true);
            setOpenChangeEnableAccountSucessAlert(false);
            setOpenFailedAlertByOutdatedData(false);
        }
        });
        setOpenChangeAccountEnable(false);
    }

    const handleChangeEnableAccountFailedAlertClose = (event?: React.SyntheticEvent, reason?: string) => {
        if (reason === 'clickaway') { return; }
        setOpenChangeEnableAccountFailedAlert(false);
    }

    const handleChangeEnableAccountSucessAlertClose = (event?: React.SyntheticEvent, reason?: string) => {
        if (reason === 'clickaway') { return; }
        setOpenChangeEnableAccountSucessAlert(false);
    }

    const handleCloseChangeAccountEnable = () => {
        setOpenChangeAccountEnable(false);
    }

    const handleResetAccountPasswordSucessAlertClose = (event?: React.SyntheticEvent, reason?: string) => {
        if (reason === 'clickaway') { return; }
        setOpenResetAccountPasswordSucessAlert(false);
    }

    const handleResetAccountPasswordFailedAlertClose = (event?: React.SyntheticEvent, reason?: string) => {
        if (reason === 'clickaway') { return; }
        setOpenResetAccountPasswordFailedAlert(false);
    }

    const handleChangeLanguageAccountFailedAlertClose = (event?: React.SyntheticEvent, reason?: string) => {
        if (reason === 'clickaway') { return; }
        setOpenChangeLanguageAccountFailedAlert(false);
    }

    const handleChangeLanguageAccountSucessAlertClose = (event?: React.SyntheticEvent, reason?: string) => {
        if (reason === 'clickaway') { return; }
        setOpenChangeLanguageAccountSucessAlert(false);
    }

    const handleChangeEmailAccountFailedAlertClose = (event?: React.SyntheticEvent, reason?: string) => {
        if (reason === 'clickaway') { return; }
        setOpenChangeEmailAccountFailedAlert(false);
    }

    const handleChangeEmailAccountSucessAlertClose = (event?: React.SyntheticEvent, reason?: string) => {
        if (reason === 'clickaway') { return; }
        setOpenChangeEmailAccountSucessAlert(false);
    }

    const updateValidationErrors = (field: string, isValid: boolean) => {
        setValidationErrors((prevErrors) => {
            const updatedErrors = { ...prevErrors };
            if (!isValid) {
                switch (field) {
                    case 'email':
                        updatedErrors[field] = t('customPaginationActionsTable.emailValidation');
                        break;
                    case 'repeated email':
                        updatedErrors[field] = t('customPaginationActionsTable.repeatEmailValidation');
                        break;
                    default:
                        break;
                }
            } else {
                delete updatedErrors[field];
            }

            if (Object.keys(updatedErrors).length === 0 && accountEmailDialog !== '' && accountEmailConfirmation !== '') {
                setIsSaveEditEmailButtonDisabled(false);
            } else {
                setIsSaveEditEmailButtonDisabled(true);
            }

            return updatedErrors;
        });
    }

    const StyledButton = styled(Button)({
        backgroundColor: '#308FFF',
        color: 'white',
        borderRadius: '5vh',
        fontSize: '0.7rem',
        fontFamily: [
            "Inter",
            'sans-serif',
        ].join(','),
    })

    const StyledBox = styled(Box)({
        display: 'inline-block',
        border: '1px solid white',
        padding: '3%',
        borderRadius: '5vh',
        color: "#1E1E1E",
        fontSize: '0.7rem',
        textTransform: 'uppercase',
        textAlign: 'center' as const,
        fontWeight: 'bold' as const,
        fontFamily: [
            "Inter",
            'sans-serif',
        ].join(','),
    })

    const StyledAdminBox = styled(StyledBox)({
        backgroundColor: '#ED6C02',
        fontcolor: '#1B2024',
        fontWeight: 'bolder',
    })

    const StyledEnabledBox = styled(StyledBox)({
        backgroundColor: '#8EEBAE',
    })

    const StyledDisabledBox = styled(StyledBox)({
        backgroundColor: '#FF5959',
    })

    const StyledRoleBox = styled(StyledBox)({
        backgroundColor: '#E2E5E3',
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
        <div>
            <TableContainer component={Paper}>
                <Table sx={{ minWidth: 500 }} aria-label="custom pagination table">
                    <TableHead>
                        <TableRow >
                            <TableCell>
                                <div className="font-link">
                                    {t('accountsListPanel.accountEmail')}
                                </div>
                            </TableCell>
                            <TableCell>
                                <div className="font-link">
                                    {t('accountsListPanel.accountRole')}
                                </div>
                            </TableCell>
                            <TableCell>
                                <div className="font-link">
                                    {t('accountsListPanel.accountStatus')}
                                </div>
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
                            <TableRow key={row.email}>
                                <TableCell component="th" scope="row">
                                    {row.email}
                                </TableCell>
                                <TableCell>
                                    {row.role === "ADMIN" ? (
                                        <StyledAdminBox>
                                            {t('accountsListPanel.adminRole')}
                                        </StyledAdminBox>
                                    ) : (
                                        <StyledRoleBox>
                                            {t('accountsListPanel.userRole')}
                                        </StyledRoleBox>
                                    )}
                                </TableCell>
                                <TableCell>
                                    {row.enable ? (
                                        <StyledEnabledBox>
                                            {t('accountsListPanel.enableStatus')}
                                        </StyledEnabledBox>
                                    ) : (
                                        <StyledDisabledBox>
                                            {t('accountsListPanel.disableStatus')}
                                        </StyledDisabledBox>
                                    )}
                                </TableCell>
                                <TableCell align="right">
                                    <IconButton onClick={() => handleAccountDetails(row.id)}>
                                        <StyledButton>
                                            {t('accountsListPanel.accountDetails')}
                                        </StyledButton>
                                    </IconButton>
                                </TableCell>
                            </TableRow>
                        ))}
                        {emptyRows > 0 && (
                            <TableRow style={{ height: 53 * emptyRows }}>
                                <TableCell colSpan={4} />
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


                <Dialog
                    open={openUserDetails}
                    onClose={handleCloseUserDetails}
                    PaperProps={{
                        style: {
                            minWidth: '50%',
                            borderRadius: '40px',
                            padding: '2%',
                            backgroundColor: '#dff2e9',
                            boxShadow: 'initial'
                        },
                    }}
                >
                    <DialogTitle id="alert-dialog-title">
                        {t('accountsListPanel.accountDetails')}
                    </DialogTitle>
                    <DialogContent>
                        <DialogContentText id="alert-dialog-description">
                            {t('accountsListPanel.accountDetailsDescription')}
                        </DialogContentText>

                        <DialogContentText id="alert-dialog-description" style={{ paddingTop: '2%' }}>
                            <i>
                                {t('accountsListPanel.accountDetailsDescription2')}
                                {t('accountsListPanel.accountDetailsDescription3')}
                                {t('accountsListPanel.accountDetailsDescription4')}
                                {t('accountsListPanel.accountDetailsDescription5')}
                            </i>
                        </DialogContentText>
                        <List style={{ paddingTop: '5%', width: '100%' }}>
                            <ListItem style={{ width: '100%' }}>
                                <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', flexDirection: 'row' }}>
                                    <div className="font-link">
                                        {t('accountsListPanel.accountEmail')}
                                    </div>
                                    <div className="paddingConatiner" style={{ paddingRight: '10%' }}>
                                        <TextField
                                            required
                                            id="outlined-required-email"
                                            style={{ backgroundColor: 'white', width: '100%' }}
                                            variant='standard'
                                            value={accountEmail}
                                            disabled
                                        />
                                    </div>
                                    {localStorage.getItem("id") !== selectedRowId.toString() && (
                                        <Button onClick={() => {
                                            setOpenEditAccountEmail(true)
                                            setAccountEmailDialog(accountEmail)
                                        }} style={dialoButtonTextStyle}>
                                            {t('accountsListPanel.editEmail')}
                                        </Button>
                                    )}
                                </div>
                            </ListItem>

                            <ListItem style={{ width: '100%' }}>
                                <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', flexDirection: 'row' }}>
                                    <div className="font-link">
                                        {t('accountsListPanel.password')}
                                    </div>
                                    <div className="paddingConatiner">
                                        {localStorage.getItem("id") !== selectedRowId.toString() && (
                                            <Button onClick={() => setOpenResetAccountPassword(true)} style={dialoButtonTextStyle}>
                                                {t('accountsListPanel.restartPassword')}
                                            </Button>
                                        )}
                                    </div>
                                </div>
                            </ListItem>

                            <ListItem style={{ display: 'flex' }}>
                                <div className="font-link">
                                    {t('accountsListPanel.accountRole')}
                                </div>
                                <div style={{ paddingLeft: '10%' }}>
                                    {accountRole === "ADMIN" ? (
                                        <StyledAdminBox>
                                            {t('accountsListPanel.adminRole')}
                                        </StyledAdminBox>
                                    ) : (
                                        <StyledRoleBox>
                                            {t('accountsListPanel.userRole')}
                                        </StyledRoleBox>
                                    )}
                                </div>
                            </ListItem>

                            <ListItem style={{ display: 'flex' }}>
                                <div className="font-link">
                                    {t('accountsListPanel.defaultLanguage')}
                                </div>
                                <div style={{ paddingLeft: '10%', paddingRight: '10%' }}>
                                    <Stack direction="row" spacing={1} alignItems="center">
                                        <Typography>
                                            <Tooltip title={t('accountsListPanel.polish')}>
                                                <img src={pl} alt="plPL" style={{ width: '40px', height: '40px' }} />
                                            </Tooltip>
                                        </Typography>
                                        <Switch
                                            checked={accountLanguage === "enUS" ? true : false}
                                            disabled />
                                        <Typography>
                                            <Tooltip title={t('accountsListPanel.english')}>
                                                <img src={gb} alt="enUS" style={{ width: '40px', height: '40px' }} />
                                            </Tooltip>
                                        </Typography>
                                    </Stack>
                                </div>
                                <Button onClick={() => {
                                    setOpenChangeAccountLanguage(true)
                                    setAccountLanguageDialog(accountLanguage)
                                }} style={dialoButtonTextStyle}>
                                    {t('accountsListPanel.editLanguage')}
                                </Button>
                            </ListItem>

                            <ListItem style={{ display: 'flex' }}>
                                <div className="font-link">
                                    {t('accountsListPanel.accountStatus')}
                                </div>
                                <div style={{ paddingLeft: '10%', paddingRight: '10%' }}>
                                    <Stack direction="row" spacing={1} alignItems="center">
                                        <Typography>{t('accountsListPanel.disableStatus')}</Typography>
                                        <Switch
                                            checked={accountEnable}
                                            disabled
                                        />
                                        <Typography>{t('accountsListPanel.enableStatus')}</Typography>
                                    </Stack>
                                </div>

                                {localStorage.getItem("id") !== selectedRowId.toString() && (
                                    <Button onClick={() => {
                                        setOpenChangeAccountEnable(true)
                                        setAccountEnableDialog(accountEnable)
                                    }} style={dialoButtonTextStyle}>
                                        {t('accountsListPanel.editStatus')}
                                    </Button>
                                )}
                            </ListItem>

                            <ListItem style={{ display: 'flex' }}>
                                <div className="font-link">
                                    {t('accountsListPanel.accountState')}
                                </div>
                                <div style={{ paddingLeft: '10%' }}>
                                    <Stack direction="row" spacing={1} alignItems="center">
                                        <Typography>{t('accountsListPanel.inactive')}</Typography>
                                        <Switch disabled
                                            checked={accountActive}
                                            onChange={handleSwitchAccountActiveChange}
                                        />
                                        <Typography>{t('accountsListPanel.active')}</Typography>
                                    </Stack>
                                </div>
                            </ListItem>

                            <ListItem style={{ display: 'flex' }}>
                                <div className="font-link">
                                    {t('accountsListPanel.registrationDate')}
                                </div>
                                <div style={{ paddingLeft: '10%' }}>
                                    <TextField
                                        required
                                        id="outlined-required-email"
                                        style={{ backgroundColor: 'white', width: '100%' }}
                                        variant='standard'
                                        value={accountRegistrationDate}
                                        disabled
                                    />
                                </div>
                            </ListItem>

                            <ListItem style={{ display: 'flex' }}>
                                <div className="font-link">
                                    {t('accountsListPanel.lastValidLoginDate')}
                                </div>
                                <div style={{ paddingLeft: '10%' }}>
                                    <TextField
                                        required
                                        id="outlined-required-email"
                                        style={{ backgroundColor: 'white', width: '100%' }}
                                        variant='standard'
                                        value={accountLastValidLoginDate}
                                        disabled
                                    />
                                </div>
                            </ListItem>

                            <ListItem style={{ display: 'flex' }}>
                                <div className="font-link">
                                    {t('accountsListPanel.lastInvalidLoginDate')}
                                </div>
                                <div style={{ paddingLeft: '10%' }}>
                                    <TextField
                                        required
                                        id="outlined-required-email"
                                        style={{ backgroundColor: 'white', width: '100%' }}
                                        variant='standard'
                                        value={accountLastInvalidLoginDate}
                                        disabled
                                    />
                                </div>
                            </ListItem>

                        </List>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={handleCloseUserDetails} style={dialoButtonTextStyle}>
                            {t('accountsListPanel.exit')}
                        </Button>
                    </DialogActions>
                </Dialog>



                <Dialog
                    open={openEditAccountEmail}
                    onClose={handleCloseEditAccountEmail}
                    PaperProps={{
                        style: {
                            borderRadius: '40px',
                            padding: '2%',
                            backgroundColor: '#bef4da',
                            boxShadow: 'initial'
                        },
                    }}
                >
                    <DialogTitle id="alert-dialog-title">
                        {t('accountsListPanel.editAccountEmail')}
                    </DialogTitle>
                    <DialogContent>
                        <DialogContentText id="alert-dialog-description">
                            {t('accountsListPanel.editAccountEmailDescription')}
                        </DialogContentText>
                        <TextField
                            variant="outlined"
                            value={accountEmailDialog}
                            onChange={handleEditAccountEmailChange}
                            style={{ width: '100%', paddingBottom: '2%', paddingTop: '8%' }}
                            InputProps={{
                                style: { backgroundColor: 'white' },
                            }}
                        />
                        <DialogContentText id="alert-dialog-description">
                            {t('accountsListPanel.editAccountEmailDescription2')}
                        </DialogContentText>
                        <TextField
                            variant="outlined"
                            value={accountEmailConfirmation}
                            onChange={handleEditAccountEmailConfirmationChange}
                            style={{ width: '100%', paddingBottom: '2%', paddingTop: '8%' }}
                            InputProps={{
                                style: { backgroundColor: 'white' },
                            }}
                        />
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={handleCloseEditAccountEmail} style={dialoButtonTextStyle}>
                            {t('accountsListPanel.exit')}
                        </Button>
                        <Button onClick={handleEditAccountEmail} disabled={isSaveEditEmailButtonDisabled} autoFocus style={dialoButtonTextStyle}>
                            {t('accountsListPanel.edit')}
                        </Button>
                    </DialogActions>

                    <Stack sx={{
                        width: '20%',
                        position: 'fixed',
                        bottom: 16 + alertHeight,
                        right: 16,
                        zIndex: 1000,
                    }} spacing={2}>
                        {Object.values(validationErrors).map((error, index) => (
                            <Alert key={index} severity="error" onLoad={handleAlertLoad}>
                                <AlertTitle>{t('customPaginationActionsTable.formValidation')}</AlertTitle>
                                {error}
                            </Alert>
                        ))}
                    </Stack>
                </Dialog>



                <Dialog
                    open={openResetAccountPassword}
                    onClose={handleCloseResetAccountPassword}
                    PaperProps={{
                        style: {
                            borderRadius: '40px',
                            padding: '2%',
                            backgroundColor: '#bef4da',
                            boxShadow: 'initial'
                        },
                    }}
                >
                    <DialogTitle id="alert-dialog-title">
                        {t('accountsListPanel.resetAccountPassword')}
                    </DialogTitle>
                    <DialogContent>
                        <DialogContentText id="alert-dialog-description">
                            {t('accountsListPanel.resetAccountPasswordDescription')}
                        </DialogContentText>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={handleCloseResetAccountPassword} style={dialoButtonTextStyle}>
                            {t('accountsListPanel.exit')}
                        </Button>
                        <Button onClick={handleResetAccountPassword} autoFocus style={dialoButtonTextStyle}>
                            {t('accountsListPanel.restartPassword')}
                        </Button>
                    </DialogActions>
                </Dialog>


                <Dialog
                    open={openChangeAccountLanguage}
                    onClose={handleCloseChangeAccountLanguage}
                    PaperProps={{
                        style: {
                            borderRadius: '40px',
                            padding: '2%',
                            backgroundColor: '#bef4da',
                            boxShadow: 'initial'
                        },
                    }}
                >
                    <DialogTitle id="alert-dialog-title">
                        {t('accountsListPanel.changeAccountLanguage')}
                    </DialogTitle>
                    <DialogContent>
                        <DialogContentText id="alert-dialog-description" style={{ paddingBottom: '8%' }}>
                            {t('accountsListPanel.changeAccountLanguageDescription')}
                        </DialogContentText>
                        <Select
                            labelId="demo-simple-select-label"
                            id="language-select"
                            value={accountLanguageDialog}
                            label="Language"
                            onChange={handleAccountLanguageChange}
                            style={{ width: '50%', }}
                        >

                            <MenuItem value={"plPL"}>
                                <img src={pl} alt="plPL" style={{ width: '20px', height: '20px' }} />
                            </MenuItem>
                            <MenuItem value={"enUS"}>
                                <img src={gb} alt="enUS" style={{ width: '20px', height: '20px' }} />
                            </MenuItem>
                        </Select>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={handleCloseChangeAccountLanguage} style={dialoButtonTextStyle}>
                            {t('accountsListPanel.exit')}
                        </Button>
                        <Button onClick={handleChangeAccountLanguage} autoFocus style={dialoButtonTextStyle}>
                            {t('accountsListPanel.edit')}
                        </Button>
                    </DialogActions>
                </Dialog>



                <Dialog
                    open={openChangeAccountEnable}
                    onClose={handleCloseChangeAccountEnable}
                    PaperProps={{
                        style: {
                            borderRadius: '40px',
                            padding: '2%',
                            backgroundColor: '#bef4da',
                            boxShadow: 'initial'
                        },
                    }}
                >
                    <DialogTitle id="alert-dialog-title">
                        {t('accountsListPanel.changeAccountEnable')}
                    </DialogTitle>
                    <DialogContent>
                        <DialogContentText id="alert-dialog-description" style={{ paddingBottom: '8%' }}>
                            {t('accountsListPanel.changeAccountEnableDescription')}
                        </DialogContentText>
                        <Select
                            labelId="demo-simple-select-label"
                            id="language-select"
                            value={accountEnableDialog}
                            label="Language"
                            onChange={handleAccountEnableChange}
                            style={{ width: '50%', }}
                        >

                            <MenuItem value={"true"}>
                                {t('accountsListPanel.enableStatus')}
                            </MenuItem>
                            <MenuItem value={"false"}>
                                {t('accountsListPanel.disableStatus')}
                            </MenuItem>
                        </Select>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={handleCloseChangeAccountEnable} style={dialoButtonTextStyle}>
                            {t('accountsListPanel.exit')}
                        </Button>
                        <Button onClick={handleChangeAccountEnable} autoFocus style={dialoButtonTextStyle}>
                            {t('accountsListPanel.edit')}
                        </Button>
                    </DialogActions>
                </Dialog>
            </TableContainer>
            <Snackbar open={openResetAccountPasswordFailedAlert} autoHideDuration={6000} onClose={handleResetAccountPasswordFailedAlertClose}>
                <Alert severity="error" sx={{ width: '100%' }} onClose={handleResetAccountPasswordFailedAlertClose}>
                    {t('customPaginationActionsTable.resetPasswordFailed')}
                </Alert>
            </Snackbar>

            <Snackbar open={openResetAccountPasswordSucessAlert} autoHideDuration={6000} onClose={handleResetAccountPasswordSucessAlertClose}>
                <Alert severity="success" sx={{ width: '100%' }} onClose={handleResetAccountPasswordSucessAlertClose}>
                    {t('customPaginationActionsTable.resetPasswordSuccess')}
                </Alert>
            </Snackbar>

            <Snackbar open={openChangeEnableAccountFailedAlert} autoHideDuration={6000} onClose={handleChangeEnableAccountFailedAlertClose}>
                <Alert severity="error" sx={{ width: '100%' }} onClose={handleChangeEnableAccountFailedAlertClose}>
                    {t('customPaginationActionsTable.changeEnableFailed')}
                </Alert>
            </Snackbar>

            <Snackbar open={openChangeEnableAccountSucessAlert} autoHideDuration={6000} onClose={handleChangeEnableAccountSucessAlertClose}>
                <Alert severity="success" sx={{ width: '100%' }} onClose={handleChangeEnableAccountSucessAlertClose}>
                    {t('customPaginationActionsTable.changeEnableSuccess')}
                </Alert>
            </Snackbar>

            <Snackbar open={openChangeLanguageAccountFailedAlert} autoHideDuration={6000} onClose={handleChangeLanguageAccountFailedAlertClose}>
                <Alert severity="error" sx={{ width: '100%' }} onClose={handleChangeLanguageAccountFailedAlertClose}>
                    {t('customPaginationActionsTable.changeLanguageFailed')}
                </Alert>
            </Snackbar>

            <Snackbar open={openChangeLanguageAccountSucessAlert} autoHideDuration={6000} onClose={handleChangeLanguageAccountSucessAlertClose}>
                <Alert severity="success" sx={{ width: '100%' }} onClose={handleChangeLanguageAccountSucessAlertClose}>
                    {t('customPaginationActionsTable.changeLanguageSuccess')}
                </Alert>
            </Snackbar>

            <Snackbar open={openChangeEmailAccountFailedAlert} autoHideDuration={6000} onClose={handleChangeEmailAccountFailedAlertClose}>
                <Alert severity="error" sx={{ width: '100%' }} onClose={handleChangeEmailAccountFailedAlertClose}>
                    {t('customPaginationActionsTable.changeEmailFailed')}
                </Alert>
            </Snackbar>

            <Snackbar open={openChangeEmailAccountSucessAlert} autoHideDuration={6000} onClose={handleChangeEmailAccountSucessAlertClose}>
                <Alert severity="success" sx={{ width: '100%' }} onClose={handleChangeEmailAccountSucessAlertClose}>
                    {t('customPaginationActionsTable.changeEmailSuccess')}
                </Alert>
            </Snackbar>

            <Snackbar open={openFailedAlertByOutdatedData} autoHideDuration={6000} onClose={handleChangeFailedByOutdatedDataAlertClose}>
                <Alert severity="error" sx={{ width: '100%' }} onClose={handleChangeFailedByOutdatedDataAlertClose}>
                    {t('common.optimisticLockError')}
                </Alert>
            </Snackbar>
        </div>
    );
}
