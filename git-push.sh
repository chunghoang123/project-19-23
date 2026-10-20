#!/usr/bin/env bash
# =============================================================================
#  Đẩy toàn bộ dự án lên GitHub - tự thử cả 2 cách.
#
#    ./git-push.sh
#
#  Cách 1 (ưu tiên): SSH - hoạt động nếu đã thêm public key vào tài khoản
#                    https://github.com/settings/ssh/new
#  Cách 2: HTTPS + Personal Access Token (Username: chunghoang123)
#
#  Lưu ý: với HTTPS, Password phải là PAT và token phải có
#  quyền "Contents: Read and write" cho repo này.
# =============================================================================
set -uo pipefail

cd "$(dirname "$0")"

REPO="chunghoang123/project-19-23"
HTTPS_URL="https://github.com/${REPO}.git"
SSH_URL="git@github.com:${REPO}.git"

echo "╔════════════════════════════════════════════════════════════╗"
echo "║  Đẩy dự án CineX lên GitHub                                 ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo
echo "Nhánh        : $(git branch --show-current)"
echo "Commit       : $(git log --oneline -1)"
echo "Số file      : $(git ls-files | wc -l)"
echo

# ---------------------------------------------------------------- SSH
if ssh -o BatchMode=yes -o ConnectTimeout=10 -T git@github.com 2>&1 | grep -q "successfully authenticated"; then
    echo "→ Phát hiện SSH key hợp lệ, chuyển sang đẩy bằng SSH"
    git remote set-url origin "$SSH_URL"
    git push -u origin main && { echo; echo "✔ Đẩy xong (SSH): https://github.com/${REPO}"; exit 0; }
    echo "✗ SSH push thất bại, chuyển sang HTTPS"
fi

# --------------------------------------------------------------- HTTPS
echo "→ Dùng HTTPS + Personal Access Token"
git remote set-url origin "$HTTPS_URL"
echo
echo "  Username : chunghoang123"
echo "  Password : Personal Access Token (PAT)"
echo "  Token cần quyền: Contents → Read and write cho repo ${REPO}"
echo "  (nếu báo 403: sửa quyền tại https://github.com/settings/personal-access-tokens)"
echo

if git push -u origin main; then
    echo
    echo "✔ Đẩy xong: https://github.com/${REPO}"
else
    echo
    echo "✗ Push thất bại. Kiểm tra 2 khả năng:"
    echo "   1. PAT chưa có quyền 'Contents: Read and write' cho repo này"
    echo "      → https://github.com/settings/personal-access-tokens"
    echo "   2. Chưa thêm SSH key → https://github.com/settings/ssh/new"
    exit 1
fi
