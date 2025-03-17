import { useTranslation } from "react-i18next";
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faCircleUser } from '@fortawesome/free-solid-svg-icons';
import { ListItemButton, ListItemText, Box, List, ListItem, TextField, Dialog, DialogTitle, DialogContent, Snackbar, DialogContentText, DialogActions, Button, Stack, Alert, AlertTitle } from '@mui/material';
import "../../i18n";
import axios from "axios";
import { API_URL } from '../../consts';
import React from "react";
import { AccountEmailChangeRequestDto } from "../../types/AccountEmailChangeRequestDto";
import { AccountPasswordChangeRequestDto } from "../../types/AccountPasswordChangeRequestDto";
import { useNavigate } from 'react-router-dom';
import { API_CALLBACK_ROUTE_CHANGE_EMAIL } from '../../consts';
import { set } from "react-hook-form";

const AccountPanel = () => {
    const { t, i18n } = useTranslation();
    const [accountEmail, setAccountEmail] = React.useState('');
    const [accountSign, setAccountSign] = React.useState('');
    const [accountVersion, setAccountVersion] = React.useState(-1);
    const [accountEmailDialog, setAccountEmailDialog] = React.useState('');
    const [accountEmailConfirmationDialog, setAccountEmailConfirmationDialog] = React.useState('');
    const [openEditAccountEmail, setOpenEditAccountEmail] = React.useState(false);
    const [validationErrors, setValidationErrors] = React.useState<string[]>([]);
    const [isSaveEditEmailButtonDisabled, setIsSaveEditEmailButtonDisabled] = React.useState(true);
    const [alertHeight, setAlertHeight] = React.useState(0);
    const [openChangeEmailAccountFailedAlert, setOpenChangeEmailAccountFailedAlert] = React.useState(false);
    const [openChangeEmailAccountFailedAlertByOutdatedData, setOpenChangeEmailAccountFailedAlertByOutdatedData] = React.useState(false);
    const [openChangeEmailAccountSucessAlert, setOpenChangeEmailAccountSucessAlert] = React.useState(false);
    const [openEditAccountPassword, setOpenEditAccountPassword] = React.useState(false);
    const [accountPasswordDialog, setAccountPasswordDialog] = React.useState('');
    const [accountPasswordConfirmationDialog, setAccountPasswordConfirmationDialog] = React.useState('');
    const [isSaveEditPasswordButtonDisabled, setIsSaveEditPasswordButtonDisabled] = React.useState(true);
    const [openChangePasswordAccountFailedAlert, setOpenChangePasswordAccountFailedAlert] = React.useState(false);
    const [openChangePasswordAccountSucessAlert, setOpenChangePasswordAccountSucessAlert] = React.useState(false);
    const [accountLastPasswordDialog, setAccountLastPasswordDialog] = React.useState('');
    const navigate = useNavigate();

    let configGetAccount = {
        method: 'GET',
        url: API_URL + '/accounts/account/' + localStorage.getItem("id"),
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + localStorage.getItem('token')
        },
    };

    const featchGetAccountData = async () => {
        axios.request(configGetAccount)
            .then((response) => {
                setAccountEmail(response.data.email);
                setAccountSign(response.data.sign);
                setAccountVersion(response.data.version);
            }).catch((error) => {
            })

    }

    React.useEffect(() => {
        featchGetAccountData();
    }, []);

    const handleAlertLoad = (event: React.ChangeEvent<HTMLInputElement>) => {
        setAlertHeight(event.target.clientHeight);
        console.log(event.target.clientHeight);
    };

    const handleCloseEditAccountPassword = () => {
        setOpenEditAccountPassword(false);
        setAccountPasswordDialog('');
        setAccountPasswordConfirmationDialog('');
        setValidationErrors([]);
    };

    const handleEditAccountPasswordChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setAccountPasswordDialog(event.target.value);
        validatePassword(event.target.value);
    }

    const handleEditAccountPasswordConfirmationDialogChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const newRepeatPassword = event.target.value.trim();
        setAccountPasswordConfirmationDialog(newRepeatPassword);
        validateRepeatPassword(accountPasswordDialog.trim(), newRepeatPassword);
    }

    const handleEditAccountLastPasswordChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setAccountLastPasswordDialog(event.target.value);
        validateLastPassword(event.target.value);
    }

    const handleChangeEmailAccountFailedAlertClose = (event?: React.SyntheticEvent, reason?: string) => {
        if (reason === 'clickaway') { return; }
        setOpenChangeEmailAccountFailedAlert(false);
    };

    const handleChangeEmailAccountFailedByOutdatedDataAlertClose = (event?: React.SyntheticEvent, reason?: string) => {
        if (reason === 'clickaway') { return; }
        setOpenChangeEmailAccountFailedAlertByOutdatedData(false);
    };

    const handleChangeEmailAccountSucessAlertClose = (event?: React.SyntheticEvent, reason?: string) => {
        if (reason === 'clickaway') { return; }
        setOpenChangeEmailAccountSucessAlert(false);
    };

    const handleChangePasswordAccountFailedAlertClose = (event?: React.SyntheticEvent, reason?: string) => {
        if (reason === 'clickaway') { return; }
        setOpenChangePasswordAccountFailedAlert(false);
    };

    const handleChangePasswordAccountSucessAlertClose = (event?: React.SyntheticEvent, reason?: string) => {
        if (reason === 'clickaway') { return; }
        setOpenChangePasswordAccountSucessAlert(false);
    };

    const handleCloseEditAccountEmail = () => {
        setOpenEditAccountEmail(false);
        setAccountEmailDialog('');
        setAccountEmailConfirmationDialog('');
        setValidationErrors([]);
    };

    const handleEditAccountEmailChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setAccountEmailDialog(event.target.value);
        validateEmail(event.target.value);
    }

    const handleEditAccountEmailConfirmationDialogChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const newRepeatEmail = event.target.value.trim();
        setAccountEmailConfirmationDialog(newRepeatEmail);
        validateRepeatEmail(accountEmailDialog.trim(), newRepeatEmail);
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

    const validateLastPassword = (password: string) => {
        const isValid = password.length > 1;
        updateValidationErrors('last password', isValid);
        return isValid;
    }

    const validatePassword = (password: string) => {
        const re = /^(?=.*\d)(?=.*[a-z])(?=.*[A-Z]).{8,}$/;
        const isValid = password.length > 8 && re.test(password);
        updateValidationErrors('password', isValid);
        return isValid;
    }

    const validateRepeatPassword = (password: string, repeatPassword: string) => {
        const isValid = password.localeCompare(repeatPassword);
        updateValidationErrors('repeated password', isValid === 0);
        return isValid;
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
                    case 'password':
                        updatedErrors[field] = t('customPaginationActionsTable.passwordValidation');
                        break;
                    case 'repeated password':
                        updatedErrors[field] = t('customPaginationActionsTable.repeatPasswordValidation');
                        break;
                    case 'last password':
                        updatedErrors[field] = t('customPaginationActionsTable.lastPasswordValidation');
                        break;
                    default:
                        break;
                }
            } else {
                delete updatedErrors[field];
            }

            if (Object.keys(updatedErrors).length === 0 && accountEmailDialog !== '' && accountEmailConfirmationDialog !== '') {
                setIsSaveEditEmailButtonDisabled(false);
            } else {
                setIsSaveEditEmailButtonDisabled(true);
            }

            if (Object.keys(updatedErrors).length === 0 && accountPasswordDialog !== '' && accountPasswordConfirmationDialog !== '' && accountLastPasswordDialog !== '') {
                setIsSaveEditPasswordButtonDisabled(false);
            } else {
                setIsSaveEditPasswordButtonDisabled(true);
            }

            return updatedErrors;
        });
    }

    const handleEditAccountEmail = () => {
        let data: AccountEmailChangeRequestDto = {
            newEmail: accountEmailDialog,
            repeatedNewEmail: accountEmailConfirmationDialog,
            version: accountVersion.toString(),
            callbackRoute: API_CALLBACK_ROUTE_CHANGE_EMAIL.toString()
        }

        let config = {
            method: 'PATCH',
            url: API_URL + '/accounts/account/' + localStorage.getItem("id") + '/email',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('token'),
                'If-Match': accountSign
            },
            data: JSON.stringify(data),
        };

        axios.request(config).then((response) => {
            setAccountEmailDialog('');
            setAccountEmailConfirmationDialog('');

            setOpenEditAccountEmail(false);
            setOpenChangeEmailAccountSucessAlert(true);
            setOpenChangeEmailAccountFailedAlert(false);
            setOpenChangeEmailAccountFailedAlertByOutdatedData(false);
            featchGetAccountData();
            localStorage.clear();
            navigate('/change-email')
        }).catch((error) => {
            if (error.response && error.response.status === 422) {
                setOpenChangeEmailAccountFailedAlertByOutdatedData(true);
                setOpenChangeEmailAccountSucessAlert(false);
                setOpenChangeEmailAccountFailedAlert(false);
            } else {
                setOpenChangeEmailAccountFailedAlert(true);
                setOpenChangeEmailAccountSucessAlert(false);
                setOpenChangeEmailAccountFailedAlertByOutdatedData(false);
            }
        });
        setOpenEditAccountEmail(false);
    }

    const handleEditAccountPassword = () => {

        let data: AccountPasswordChangeRequestDto = {
            lastPassword: accountLastPasswordDialog,
            newPassword: accountPasswordDialog,
            repeatedNewPassword: accountPasswordConfirmationDialog,
            version: accountVersion
        }

        let config = {
            method: 'PATCH',
            url: API_URL + '/accounts/account/' + localStorage.getItem("id") + '/password',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('token'),
                'If-Match': accountSign
            },
            data: JSON.stringify(data),
        };

        axios.request(config).then((response) => {
            setAccountLastPasswordDialog('');
            setAccountPasswordDialog('');
            setAccountPasswordConfirmationDialog('');

            setOpenEditAccountPassword(false);
            setOpenChangePasswordAccountSucessAlert(true);
            setOpenChangePasswordAccountFailedAlert(false);
            setOpenChangeEmailAccountFailedAlertByOutdatedData(false);
            featchGetAccountData();
        }).catch((error) => {
            if (error.response && error.response.status === 422) {
                setOpenChangeEmailAccountFailedAlertByOutdatedData(true);
                setOpenChangeEmailAccountSucessAlert(false);
                setOpenChangePasswordAccountFailedAlert(false);
            } else {
                setOpenChangePasswordAccountFailedAlert(true);
                setOpenChangePasswordAccountSucessAlert(false);
                setOpenChangeEmailAccountFailedAlertByOutdatedData(false);
            }
        });
        setOpenEditAccountPassword(false);
    }

    const containerStyle = {
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100%',
        width: '100%',
    };

    const boxStyle = {
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        alignItems: 'center',
        width: '50%',
        paddingTop: '10vh',
    };

    const headerTextStyle = {
        textAlign: 'center' as const,
        fontSize: '4.0rem',
        color: 'rgba(43, 42, 42, 0.7)',
        paddingTop: '5vh',
    };

    const iconStyle = {
        color: '#99EDC5',
        fontSize: 100,
        paddingRight: '1vw',
    };

    const listStyle = {
        width: '80%',
        aliginItems: 'center',
        justifyContent: 'center'
    };

    const buttonTextStyle = {
        textAlign: 'center' as const,
        backgroundColor: 'rgba(131, 225, 180, 1)',
        borderRadius: '5vh',
        fontSize: '1.5rem',
        color: 'rgba(245, 249, 246, 1)',
    };

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
            <div className="font-link" style={headerTextStyle}>
                <span>
                    <FontAwesomeIcon icon={faCircleUser} style={iconStyle} />
                    {t('accountPanel.account')}
                </span>
            </div>

            <div style={containerStyle}>
                <Box sx={boxStyle}>
                    <List style={listStyle}>
                        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', flexDirection: 'column' }}>
                            <ListItem className="font-link-header">
                                <div>
                                    {t('accountPanel.changeEmail')}
                                </div>
                            </ListItem>
                            <ListItem className="font-link">
                                <div>
                                    {t('accountPanel.addressEmail')}
                                </div>
                            </ListItem>
                            <ListItem style={{ display: 'flex', justifyContent: 'center' }}>
                                <TextField
                                    disabled
                                    id="outlined-required-email"
                                    style={{ backgroundColor: 'white', width: '100%' }}
                                    variant='standard'
                                    value={accountEmail}
                                />
                            </ListItem>
                            <ListItem>
                                <ListItemButton onClick={() => setOpenEditAccountEmail(true)}>
                                    <ListItemText>
                                        <div className="font-link" style={buttonTextStyle}>
                                            {t('accountPanel.changeEmailButton')}
                                        </div>
                                    </ListItemText>
                                </ListItemButton>
                            </ListItem>

                            <ListItem className="font-link-header">
                                <div>
                                    {t('accountPanel.changePassword')}
                                </div>
                            </ListItem>
                            <ListItem className="font-link">
                                <div>
                                    {t('accountPanel.password')}
                                </div>
                            </ListItem>
                            <ListItem style={{ display: 'flex', justifyContent: 'center' }}>
                                <TextField
                                    disabled
                                    id="outlined-required-email"
                                    style={{ backgroundColor: 'white', width: '100%' }}
                                    variant='standard'
                                />
                            </ListItem>
                            <ListItem>
                                <ListItemButton onClick={() => setOpenEditAccountPassword(true)}>
                                    <ListItemText>
                                        <div className="font-link" style={buttonTextStyle}>
                                            {t('accountPanel.changePasswordButton')}
                                        </div>
                                    </ListItemText>
                                </ListItemButton>
                            </ListItem>
                        </div>
                    </List>
                </Box>
            </div>


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
                    {t('accountPanel.editAccountEmail')}
                </DialogTitle>
                <DialogContent>
                    <DialogContentText id="alert-dialog-description">
                        {t('accountPanel.editAccountEmailDescription')}*
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
                        {t('accountPanel.editAccountEmailDescription2')}*
                    </DialogContentText>
                    <TextField
                        variant="outlined"
                        value={accountEmailConfirmationDialog}
                        onChange={handleEditAccountEmailConfirmationDialogChange}
                        style={{ width: '100%', paddingBottom: '2%', paddingTop: '8%' }}
                        InputProps={{
                            style: { backgroundColor: 'white' },
                        }}
                    />
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleCloseEditAccountEmail} style={dialoButtonTextStyle}>
                        {t('accountPanel.exit')}
                    </Button>
                    <Button onClick={handleEditAccountEmail} disabled={isSaveEditEmailButtonDisabled} autoFocus style={dialoButtonTextStyle}>
                        {t('accountPanel.edit')}
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
                            <AlertTitle>{t('accountPanel.formValidation')}</AlertTitle>
                            {error}
                        </Alert>
                    ))}
                </Stack>
            </Dialog>


            <Snackbar open={openChangeEmailAccountFailedAlert} autoHideDuration={6000} onClose={handleChangeEmailAccountFailedAlertClose}>
                <Alert severity="error" sx={{ width: '100%' }} onClose={handleChangeEmailAccountFailedAlertClose}>
                    {t('accountPanel.changeEmailFailed')}
                </Alert>
            </Snackbar>

            <Snackbar open={openChangeEmailAccountFailedAlertByOutdatedData} autoHideDuration={6000} onClose={handleChangeEmailAccountFailedByOutdatedDataAlertClose}>
                <Alert severity="error" sx={{ width: '100%' }} onClose={handleChangeEmailAccountFailedByOutdatedDataAlertClose}>
                    {t('common.optimisticLockError')}
                </Alert>
            </Snackbar>

            <Snackbar open={openChangeEmailAccountSucessAlert} autoHideDuration={6000} onClose={handleChangeEmailAccountSucessAlertClose}>
                <Alert severity="success" sx={{ width: '100%' }} onClose={handleChangeEmailAccountSucessAlertClose}>
                    {t('accountPanel.changeEmailSuccess')}
                </Alert>
            </Snackbar>



            <Dialog
                open={openEditAccountPassword}
                onClose={handleCloseEditAccountPassword}
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
                    {t('accountPanel.editAccountPassword')}
                </DialogTitle>
                <DialogContent>
                    <DialogContentText id="alert-dialog-description">
                        {t('accountPanel.editAccountPasswordDescription')}*
                    </DialogContentText>
                    <TextField
                        variant="outlined"
                        value={accountLastPasswordDialog}
                        onChange={handleEditAccountLastPasswordChange}
                        style={{ width: '100%', paddingBottom: '2%', paddingTop: '8%' }}
                        InputProps={{
                            style: { backgroundColor: 'white' },
                        }}
                        type="password"
                    />
                    <DialogContentText id="alert-dialog-description">
                        {t('accountPanel.editAccountPasswordDescription1')}*
                    </DialogContentText>
                    <TextField
                        variant="outlined"
                        value={accountPasswordDialog}
                        onChange={handleEditAccountPasswordChange}
                        style={{ width: '100%', paddingBottom: '2%', paddingTop: '8%' }}
                        InputProps={{
                            style: { backgroundColor: 'white' },
                        }}
                        type="password"
                    />
                    <DialogContentText id="alert-dialog-description">
                        {t('accountPanel.editAccountPasswordDescription2')}*
                    </DialogContentText>
                    <TextField
                        variant="outlined"
                        value={accountPasswordConfirmationDialog}
                        onChange={handleEditAccountPasswordConfirmationDialogChange}
                        style={{ width: '100%', paddingBottom: '2%', paddingTop: '8%' }}
                        InputProps={{
                            style: { backgroundColor: 'white' },
                        }}
                        type="password"
                    />
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleCloseEditAccountPassword} style={dialoButtonTextStyle}>
                        {t('accountPanel.exit')}
                    </Button>
                    <Button onClick={handleEditAccountPassword} disabled={isSaveEditPasswordButtonDisabled} autoFocus style={dialoButtonTextStyle}>
                        {t('accountPanel.edit')}
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
                            <AlertTitle>{t('accountPanel.formValidation')}</AlertTitle>
                            {error}
                        </Alert>
                    ))}
                </Stack>
            </Dialog>

            <Snackbar open={openChangePasswordAccountFailedAlert} autoHideDuration={6000} onClose={handleChangePasswordAccountFailedAlertClose}>
                <Alert severity="error" sx={{ width: '100%' }} onClose={handleChangePasswordAccountFailedAlertClose}>
                    {t('accountPanel.changePasswordFailed')}
                </Alert>
            </Snackbar>

            <Snackbar open={openChangePasswordAccountSucessAlert} autoHideDuration={6000} onClose={handleChangePasswordAccountSucessAlertClose}>
                <Alert severity="success" sx={{ width: '100%' }} onClose={handleChangePasswordAccountSucessAlertClose}>
                    {t('accountPanel.changePasswordSuccess')}
                </Alert>
            </Snackbar>
        </div>
    );
}

export default AccountPanel;