
int is_iOS_13x_available() {
    if (@available(iOS 13.0, *)) {
        return 1;
    }
    
    return 0;
}
