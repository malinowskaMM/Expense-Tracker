import * as React from 'react';
import Box from '@mui/material/Box';
import { useTranslation } from "react-i18next";
import "./AccountTabPanel.css";
import { FormControl, List, ListItem, TextField, Select, MenuItem, Button, Stack, Alert, Snackbar, AlertTitle, Backdrop, CircularProgress } from '@mui/material';
import { set, useForm } from "react-hook-form";
import { AccountAdminRegisterRequestDto } from '../../types/AccountAdminRegisterRequestDto';
import axios from "axios";
import { API_URL, API_CALLBACK_ROUTE_ADMIN_REGISTER } from '../../consts';

export default function AccountTabPanel() {
    const { t, i18n } = useTranslation();
    const pl = require("../../assets/pl.svg").default;
    const gb = require("../../assets/gb.svg").default;
    const { register, handleSubmit } = useForm<AccountAdminRegisterRequestDto>();
    const [email, setEmail] = React.useState<string>("")
    const [repeatEmail, setRepeatEmail] = React.useState<string>("")
    const [language, setLanguage] = React.useState<string>("plPL")
    const [validationErrors, setValidationErrors] = React.useState<string[]>([]);
    const [isAdminRegisterButtonDisabled, setIsAdminRegisterButtonDisabled] = React.useState<boolean>(true);
    const [openRegistrationFailedAlert, setOpenRegistrationFailedAlert] = React.useState(false);
    const [openRegistrationSuccessAlert, setOpenRegistrationSuccessAlert] = React.useState(false);
    const [loadingBackdropOpen, setLoadingBackdropOpen] = React.useState(false);
    const [alertHeight, setAlertHeight] = React.useState(0);

    const onSubmit = handleSubmit((data: AccountAdminRegisterRequestDto) => {
        setLoadingBackdropOpen(true);
        data.callbackRoute = API_CALLBACK_ROUTE_ADMIN_REGISTER.toString();
        data.language_ = language;

        let config = {
            method: 'POST',
            url: API_URL + '/auth/account/admin/register',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('token') || '',
            },
            data: JSON.stringify(data),
        };


        axios.request(config)
            .then((response) => {
                setLoadingBackdropOpen(false);
                setOpenRegistrationFailedAlert(false);
                setOpenRegistrationSuccessAlert(true);
            })
            .catch((error) => {
                setLoadingBackdropOpen(false);
                setOpenRegistrationFailedAlert(true);
                setOpenRegistrationSuccessAlert(false);
            });

    });

    const handleRegister = () => {
        onSubmit();
    }

    const handleEmailChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setEmail(event.target.value);
        validateEmail(event.target.value);
    };

    const handleRepeatEmailChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const newRepeatEmail = event.target.value.trim();
        setRepeatEmail(newRepeatEmail);
        validateRepeatEmail(email.trim(), newRepeatEmail);
    }

    const handleLanguageChange = (event: SelectChangeEvent) => {
        setLanguage(event.target.value as string);
        validateLanguage(event.target.value as string);
    };

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

    const validateLanguage = (language: string) => {
        const isValid = language !== '';
        updateValidationErrors('language', isValid);
        return isValid;
    }

    const handleAlertLoad = (event: React.ChangeEvent<HTMLInputElement>) => {
        setAlertHeight(event.target.clientHeight);
        console.log(event.target.clientHeight);
    };

    const updateValidationErrors = (field: string, isValid: boolean) => {
        setValidationErrors((prevErrors) => {
            const updatedErrors = { ...prevErrors };
            if (!isValid) {
                switch (field) {
                    case 'email':
                        updatedErrors[field] = t('accountTabPanel.emailValidation');
                        break;
                    case 'repeated email':
                        updatedErrors[field] = t('accountTabPanel.repeatEmailValidation');
                        break;
                    case 'language':
                        updatedErrors[field] = t('accountTabPanel.languageValidation');
                        break;    
                    default:
                        break;
                }
            } else {
                delete updatedErrors[field];
            }

            if (Object.keys(updatedErrors).length === 0 && email !== '' && repeatEmail !== '' && language !== '') {
                setIsAdminRegisterButtonDisabled(false);
            } else {
                setIsAdminRegisterButtonDisabled(true);
            }

            return updatedErrors;
        });
    }

    const handleRegistrationFailedAlertClose = (event?: React.SyntheticEvent, reason?: string) => {
        if (reason === 'clickaway') {
            return;
        }
        setOpenRegistrationFailedAlert(false);
    };

    const handleRegistrationSuccessAlertClose = (event?: React.SyntheticEvent, reason?: string) => {
        if (reason === 'clickaway') {
            return;
        }
        setOpenRegistrationSuccessAlert(false);
    };

    const buttonTextStyle = {
        textAlign: 'center' as const,
        backgroundColor: 'rgba(131, 225, 180, 1)',
        borderRadius: '5vh',
        color: '#1B2024',
        padding: '1vh',
    };


    return (
        <div>
            <Box sx={{ width: '100%' }}>
                <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
                    <FormControl>
                        <List>
                            <Box component="form" onSubmit={onSubmit}>
                                <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', flexDirection: 'column' }}>
                                    <ListItem>
                                        <div className="font-link">
                                            {t('accountTabPanel.newAdminAcount')}
                                        </div>
                                    </ListItem>

                                    <ListItem>
                                        <div className="font-link">
                                            {t('accountTabPanel.email')}*
                                        </div>
                                    </ListItem>
                                    <ListItem style={{ display: 'flex', justifyContent: 'center' }}>
                                        <TextField
                                            {...register("email")}
                                            required
                                            id="outlined-required-email"
                                            style={{ backgroundColor: 'white', width: '100%' }}
                                            variant='standard'
                                            onChange={handleEmailChange}
                                        />
                                    </ListItem>

                                    <ListItem>
                                        <div className="font-link">
                                            {t('accountTabPanel.repeatEmail')}*
                                        </div>
                                    </ListItem>
                                    <ListItem style={{ display: 'flex', justifyContent: 'center' }}>
                                        <TextField
                                            {...register("repeatEmail")}
                                            required
                                            id="outlined-required-email"
                                            style={{ backgroundColor: 'white', width: '100%' }}
                                            variant='standard'
                                            onChange={handleRepeatEmailChange}
                                        />
                                    </ListItem>

                                    <ListItem>
                                        <div className="font-link">
                                            {t('accountTabPanel.language')}*
                                        </div>
                                    </ListItem>
                                    <ListItem style={{ display: 'flex' }}>
                                        <Select
                                            labelId="demo-simple-select-label"
                                            id="language-select"
                                            value={language}
                                            label="Language"
                                            onChange={handleLanguageChange}
                                            style={{ width: '50%' }}
                                        >

                                            <MenuItem value={"plPL"}>
                                                <img src={pl} alt="plPL" style={{ width: '10%', height: '10%' }} />
                                            </MenuItem>
                                            <MenuItem value={"enUS"}>
                                                <img src={gb} alt="enUS" style={{ width: '10%', height: '10%' }} />
                                            </MenuItem>
                                        </Select>
                                    </ListItem>
                                </div>
                            </Box>
                        </List>
                    </FormControl>
                </Box>

            </Box>
            <Button disabled={isAdminRegisterButtonDisabled}>
                <div className="font-link" style={buttonTextStyle} onClick={handleRegister}>
                    {t('accountTabPanel.register')}
                </div>
            </Button>

            <Stack sx={{
                    width: '20%',
                    position: 'fixed',
                    bottom: 16 + alertHeight,
                    right: 16,
                    zIndex: 1000,
                }} spacing={2}>
                    {Object.values(validationErrors).map((error, index) => (
                        <Alert key={index} severity="error" onLoad={handleAlertLoad}>
                            <AlertTitle>{t('accountTabPanel.formValidation')}</AlertTitle>
                            {error}
                        </Alert>
                    ))}
                </Stack>

                <Snackbar open={openRegistrationFailedAlert} autoHideDuration={6000} onClose={handleRegistrationFailedAlertClose}>
                    <Alert severity="error" sx={{ width: '100%' }} onClose={handleRegistrationFailedAlertClose}>
                        {t('accountTabPanel.registrationFailed')}
                    </Alert>
                </Snackbar>

                <Snackbar open={openRegistrationSuccessAlert} autoHideDuration={6000} onClose={handleRegistrationSuccessAlertClose}>
                    <Alert severity="success" sx={{ width: '100%' }} onClose={handleRegistrationSuccessAlertClose}>
                        {t('accountTabPanel.registrationSuccess')}
                    </Alert>
                </Snackbar>

            <Backdrop
                sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
                open={loadingBackdropOpen}
            >
                <CircularProgress color="inherit" />
            </Backdrop>
        </div>
    );
}
