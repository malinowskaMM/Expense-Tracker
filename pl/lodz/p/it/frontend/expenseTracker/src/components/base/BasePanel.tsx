import { Box, Button, Grid } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from "react-i18next";

const BasePanel = () => {
    const bigLogo = require('../../assets/bigLogo.svg').default;
    const { t, i18n } = useTranslation();
    const navigate = useNavigate();

    const containerStyle = {
        backgroundColor: '#99EDC5',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
    };

    const boxStyle = {
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        alignItems: 'center',
        overflow: 'auto',
        width: '100%',
        height: '100%'
    };

    const buttonTextStyle = {
        textAlign: 'center' as const,
        backgroundColor: 'transparent',
        padding: '1vh 2vh',
        borderRadius: '5vh',
        fontSize: '1.5rem',
        color: '#1B2024',
        fontWeight: 'bolder',
    };

    const handleLogin = (event: React.SyntheticEvent<unknown>, reason?: string) => {
        navigate('/login');
    }

    const handleRegister = (event: React.SyntheticEvent<unknown>, reason?: string) => {
        navigate('/register');
    }

    return (
        <div style={{backgroundColor: '#99EDC5'}}>
            <div style={containerStyle} className="font-link">
                <Box sx={boxStyle}>
                    <img src={bigLogo} alt='logo' style={{ width: '35%' }} />
                </Box>
            </div>
            {localStorage.getItem("token") === null  && (
            <Box style={containerStyle}>
                <Grid container spacing={8} justifyContent="center" alignItems="center" >
                    <Grid item>
                        <Button>
                            <div className="font-link" style={buttonTextStyle} onClick={handleLogin}>
                                {t('loginPanel.login')}
                            </div>
                        </Button>
                    </Grid>
                    <Grid item>
                        <Button>
                            <div className="font-link" style={buttonTextStyle} onClick={handleRegister}>
                                {t('loginPanel.register')}
                            </div>
                        </Button>
                    </Grid>
                </Grid>
            </Box>
            )}
        </div>
    );


}

export default BasePanel;