import { useTranslation } from "react-i18next";
import { ListItemButton, ListItemText, Backdrop, CircularProgress, Box, List, ListItem } from '@mui/material';
import "../../i18n";
import { useParams } from "react-router-dom";
import { useEffect, useState } from 'react';
import { API_URL } from '../../consts';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

const AccountActivatedPanel = () => {
    const token = useParams().token;
    const { t, i18n } = useTranslation();
    const logo = require("../../assets/logo.svg").default;
    const [success, setSuccess] = useState(true);
    const navigate = useNavigate();
    const [loadingBackdropOpen, setLoadingBackdropOpen] = useState(true);

    useEffect(() => {
        setLoadingBackdropOpen(true);
        let tokenRequestDto = {
            token: token,
        };

        let config = {
            method: 'POST',
            url: API_URL + '/auth/account/register/confirm',
            headers: {
                'Content-Type': 'application/json'
            },
            data: JSON.stringify(tokenRequestDto),
        };

        axios(config)
            .then(function (response) {
                console.log(JSON.stringify(response.data));
                    setLoadingBackdropOpen(false);
                    setSuccess(true);
            })
            .catch(function (error) {
                setLoadingBackdropOpen(false);
                setSuccess(false);
            });
    }, []);


    const containerStyle = {
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100vh',
        width: '100vw',
    };

    const boxStyle = {
        backgroundColor: 'rgba(153, 237, 197, 0.5)',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        alignItems: 'center',
        width: '40%',
        height: '70%',
        borderRadius: '5vh',
    };

    const listStyle = {
        width: '100%',
        aliginItems: 'center',
        justifyContent: 'center',
    };

    const buttonTextStyle = {
        textAlign: 'center' as const,
        backgroundColor: 'rgba(131, 225, 180, 1)',
        borderRadius: '5vh',
        fontSize: '1.5rem',
        color: 'rgba(245, 249, 246, 1)',
    };

    const imageStyle = {
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center'
    };

    const headerTextStyle = {
        textAlign: 'center' as const,
        fontSize: '3.0rem',
        color: 'rgba(43, 42, 42, 0.8)',

    };


    return (
        <div style={containerStyle} className="font-link">
            <Box sx={boxStyle}>
                <div style={imageStyle}>
                    <img src={logo} alt='logo' style={{ width: '25%' }} />
                </div>

                <div>
                    {success &&
                        <div style={headerTextStyle}>
                            {t('accountActivatedPanel.accountActivated')}
                        </div>
                    }
                    {!success &&
                        <div style={headerTextStyle}>
                            {t('accountActivatedPanel.accountActivatedError')}
                        </div>
                    }

                    <List style={listStyle}>
                        <div style={{  width: '100%', justifyContent: 'center', alignItems: 'center'}}>
                            <ListItem>
                                {success &&
                                    <div>
                                        {t('accountActivatedPanel.accountActivatedDescription')}
                                    </div>
                                }
                                {!success &&
                                    <div>
                                        {t('accountActivatedPanel.accountActivatedDescriptionError')}
                                    </div>
                                }
                            </ListItem>
                            <ListItem>
                                {success &&
                                    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', flexDirection: 'column' }}>
                                        {t('accountActivatedPanel.accountActivatedDescription2')}
                                    </div>
                                }
                            </ListItem>
                        </div>

                        <ListItem>
                            {success &&
                                <ListItemButton>
                                    <ListItemText>
                                        <div className="font-link" style={buttonTextStyle} onClick = {() => navigate('/login')}>
                                            {t('accountActivatedPanel.login')}
                                        </div>
                                    </ListItemText>
                                </ListItemButton>
                            }
                        </ListItem>
                    </List>
                </div>
            </Box>

            <Backdrop
                sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
                open={loadingBackdropOpen}
            >
                <CircularProgress color="inherit" />
            </Backdrop>
        </div>
    );
}

export default AccountActivatedPanel;