import { useTranslation } from "react-i18next";
import { ListItemButton, ListItemText, Backdrop, CircularProgress, List, ListItem, Box } from '@mui/material';
import "../../i18n";
import { useParams } from "react-router-dom";
import { useEffect, useState } from 'react';
import { API_URL } from '../../consts';
import axios from 'axios';
import { TokenEmailRequestDto } from '../../types/TokenEmailRequestDto';
import { useNavigate } from 'react-router-dom';

const AccountEmailChangedPanel = () => {
    const { t, i18n } = useTranslation();
    const logo = require("../../assets/logo.svg").default;
    const token = useParams().token;
    const email = useParams().email;
    const id = useParams().id;
    const [success, setSuccess] = useState(true);
    const [loadingBackdropOpen, setLoadingBackdropOpen] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        setLoadingBackdropOpen(true);
        let tokenRequestDto : TokenEmailRequestDto = {
            token: token,
            newEmail: email,
        };

        let config = {
            method: 'POST',
            url: API_URL + '/accounts/account/' + id + '/email/confirm',
            headers: {
                'Content-Type': 'application/json'
            },
            data: JSON.stringify(tokenRequestDto),
        };

        axios(config)
            .then(function (response) {
                console.log(JSON.stringify(response.data));
                if (response.data.status === 200) {
                    setLoadingBackdropOpen(false);
                    setSuccess(true);
                }
            })
            .catch(function (error) {
                setLoadingBackdropOpen(false);
                setSuccess(false);
            });

            setLoadingBackdropOpen(false);

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

                {success &&
                <div style={headerTextStyle}>
                    {t('accountEmailChangedPanel.accountEmailChanged')}
                </div>
                }
                {!success &&
                <div style={headerTextStyle}>
                    {t('accountEmailChangedPanel.accountEmailNotChanged')}
                </div>
                }

                <List style={listStyle}>
                    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', flexDirection: 'column' }}>
                        <ListItem>
                        {success &&
                            <div>
                                {t('accountEmailChangedPanel.accountEmailChangedDescription')}
                            </div>
                        }
                        {!success &&
                            <div>
                                {t('accountEmailChangedPanel.accountEmailNotChangedDescription')}
                            </div>
                        }
                        </ListItem>
                    </div>

                    <ListItem>
                        <ListItemButton>
                            <ListItemText>
                            {success &&
                                <div className="font-link" style={buttonTextStyle} onClick={() => navigate('/login')}>
                                    {t('accountEmailChangedPanel.login')}
                                </div>
                            }
                            </ListItemText>
                        </ListItemButton>
                    </ListItem>
                </List>
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

export default AccountEmailChangedPanel;